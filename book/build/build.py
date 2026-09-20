"""Builds the book as PDF (LaTeX), EPUB and HTML with Pandoc (through Docker; nothing else to install).

Run from anywhere:   python book/build/build.py [--no-pdf] [--no-epub] [--no-html] [--only=word,word]
Output:              book/build/out/secure-doc-viewer-guide.pdf, .epub and .html

Steps
 1. The manuscript is assembled (out/manuscript.md): files are joined in the order of order.txt with a
    clean break between them, line endings are normalized and the HTML comments that carry chapter
    metadata and source notes are removed (a comment directly after a list can make Pandoc swallow the
    next heading).
 2. Mermaid diagrams are rendered to PNG (mermaid-cli + host Chrome). Rendering is slow, so it is
    skipped when the diagram sources have not changed since the last run.
 3. Each diagram gets alternative text: the "Text description" paragraph under it (falling back to the
    caption). Missing descriptions are listed as warnings.
 4. Pandoc runs in a Docker image with real fonts (see Dockerfile). The PDF is typeset with LuaLaTeX
    as a tagged PDF/UA-2 book (header.tex holds the layout); EPUB and HTML share book.lua, book.css
    and the accessibility metadata. Then table headers get scope attributes.
"""
import hashlib
import os
import re
import shutil
import subprocess
import sys
import zipfile

BACKSLASH = chr(92)
ARGS = set(sys.argv[1:])
# Preview mode: --only=13-valid,17-files builds just the files whose path contains one of the words, into out/preview
# (a couple of minutes instead of the full build). The final book is always a full build.
ONLY = next((a.split('=', 1)[1] for a in sys.argv[1:] if a.startswith('--only=')), None)
if ONLY:
    ARGS.update({'--no-epub', '--no-html'})

HERE = os.path.dirname(os.path.abspath(__file__))
BOOK = os.path.dirname(HERE)
OUT = os.path.join(HERE, 'out', 'preview') if ONLY else os.path.join(HERE, 'out')
OUTC = '/data/build/out' + ('/preview' if ONLY else '')   # the same folder as seen inside the container
DIAGRAMS = os.path.join(OUT, 'diagrams')
os.makedirs(DIAGRAMS, exist_ok=True)
# ---- 1. manuscript -------------------------------------------------------------------------------
parts = []
names = open(os.path.join(HERE, 'order.txt'), encoding='utf-8').read().split()
if ONLY:
    names = [n for n in names if any(k in n for k in ONLY.split(','))]
    print('preview of:', ', '.join(names))
for name in names:
    text = open(os.path.join(BOOK, name), encoding='utf-8').read().replace('\r\n', '\n')
    text = re.sub(r'<!--.*?-->[ \t]*\n?', '', text, flags=re.S)
    parts.append(text.strip('\n'))
manuscript = '\n\n'.join(parts) + '\n'
with open(os.path.join(OUT, 'manuscript.md'), 'w', encoding='utf-8', newline='\n') as f:
    f.write(manuscript)

# ---- 2. diagrams (cached on the diagram sources) ---------------------------------------------------
blocks = re.findall(r'```mermaid\n.*?```', manuscript, flags=re.S)
digest = hashlib.sha256('\n'.join(blocks).encode('utf-8')).hexdigest()
stamp = os.path.join(DIAGRAMS, 'sources.sha256')
raw = os.path.join(DIAGRAMS, 'rendered-raw.md')
cached = os.path.exists(raw) and os.path.exists(stamp) and open(stamp).read().strip() == digest
if cached:
    print(f'diagrams unchanged ({len(blocks)}), skipping the render')
else:
    env0 = dict(os.environ, PUPPETEER_SKIP_DOWNLOAD='1')
    subprocess.run('npx --yes -p @mermaid-js/mermaid-cli mmdc -p "' + os.path.join(HERE, 'puppeteer-config.json').replace(BACKSLASH, '/') + '" -i manuscript.md '
                   '-o diagrams/rendered.md -e png -s 2 -b white', cwd=OUT, check=True, shell=True, env=env0)
    shutil.copyfile(os.path.join(DIAGRAMS, 'rendered.md'), raw)
    with open(stamp, 'w') as f:
        f.write(digest)

# ---- 3. alternative text -------------------------------------------------------------------------
# The text always comes from the CURRENT manuscript: each mermaid block is replaced by a link to the image
# mermaid-cli made for it (rendered-1.png, rendered-2.png ... in order). Only the images are cached.
_n = [0]


def _image_link(_m):
    _n[0] += 1
    return f'![diagram](./rendered-{_n[0]}.png)'


lines = re.sub(r'```mermaid\n.*?```', _image_link, manuscript, flags=re.S).split('\n')
missing = []
for i, line in enumerate(lines):
    m = re.match(r'!\[diagram\]\((.+?)\)$', line)
    if not m:
        continue
    caption = desc = None
    for nxt in lines[i + 1:i + 8]:
        s = nxt.strip()
        if not s:
            continue
        cm = re.match(r'\*(Figure [^*]+)\*\s*$', s)
        dm = re.match(r'\*Text description:\*\s*(.+)$', s)
        if cm and caption is None:
            caption = cm.group(1)
        elif dm:
            desc = dm.group(1)
            break
        elif caption is not None:
            break
    if desc is None and caption is not None:
        missing.append(caption)
    alt = (desc or caption or 'Diagram')
    alt = re.sub(r'[\[\]]', '', re.sub(r'[*_`]', '', alt))
    lines[i] = f'![{alt}]({m.group(1)})'
web_text = '\n'.join(lines)
# PDF only: no font in the image has colour emoji, so print the one emoji (in a Java string) as its escape.
pdf_text = web_text.replace('\U0001F600', BACKSLASH + 'uD83D' + BACKSLASH + 'uDE00')


def wrap_code_blocks(text, width=96):
    """PDF only: break long code lines in the source text (with a continuation mark).

    LaTeX's own line breaking inside code blocks cannot be used because it is not compatible with
    tagged PDF. The web editions keep the code exactly as written.
    """
    out, in_code = [], False
    for line in text.split('\n'):
        if line.lstrip().startswith('```'):   # fences inside list items are indented
            in_code = not in_code
            out.append(line)
            continue
        if not in_code or len(line) <= width:
            out.append(line)
            continue
        indent = len(line) - len(line.lstrip(' '))
        cont = ' ' * (indent + 4) + '↪ '
        cur = line
        while len(cur) > width:
            cut = cur.rfind(' ', indent + 12, width)
            if cut == -1:
                cut = width
            out.append(cur[:cut].rstrip())
            cur = cont + cur[cut:].lstrip()
        out.append(cur)
    return '\n'.join(out)


pdf_text = wrap_code_blocks(pdf_text)
for name, txt in (('rendered-web.md', web_text), ('rendered-pdf.md', pdf_text)):
    with open(os.path.join(DIAGRAMS, name), 'w', encoding='utf-8', newline='\n') as f:
        f.write(txt)
if missing:
    print(f'WARNING: {len(missing)} figure(s) have no "Text description" paragraph (alt text falls back to the caption):')
    for c in missing:
        print('   ', c)

# ---- 4. Pandoc ------------------------------------------------------------------------------------
subprocess.run(['docker', 'build', '-q', '-t', 'sdv-book-pandoc', HERE], check=True)
metadata_file = os.path.join(HERE, 'metadata.yaml')
extra_meta = []
if os.path.exists(metadata_file):
    # Blank values (author, edition, licence ... not yet supplied) would make empty, invalid EPUB/PDF
    # metadata elements, so only the filled-in keys are passed on.
    kept = []
    for raw_line in open(metadata_file, encoding='utf-8').read().split('\n'):
        m = re.match(r'^([\w-]+):\s*"([^"]*)"', raw_line)
        if m and m.group(2).strip():
            kept.append(f'{m.group(1)}: "{m.group(2)}"')
    with open(os.path.join(OUT, 'metadata-filled.yaml'), 'w', encoding='utf-8', newline='\n') as f:
        f.write('\n'.join(kept) + '\n')
    extra_meta = ['--metadata-file=' + OUTC + '/metadata-filled.yaml']
common = ['docker', 'run', '--rm', '-v', BOOK.replace(BACKSLASH, '/') + ':/data', '-w', OUTC,
          'sdv-book-pandoc', '--from', 'gfm', '--standalone', '--resource-path=.:' + OUTC + '/diagrams',
          '--metadata', 'title=Building a Secure Document Viewer',
          '--metadata', 'subtitle=From first line of Java to production', '--metadata', 'lang=en-US',
          '--lua-filter=/data/build/book.lua',
          '--syntax-highlighting=/data/build/contrast.theme'] + extra_meta
env = dict(os.environ, MSYS_NO_PATHCONV='1')
WEB = 'diagrams/rendered-web.md'
PDF = 'diagrams/rendered-pdf.md'


def add_scope(html):
    """Header cells in a table head get scope="col" (screen readers use it to announce columns)."""
    def head(m):
        return re.sub(r'<th(?=[\s>])(?![^>]*\bscope=)', '<th scope="col"', m.group(0))
    return re.sub(r'<thead.*?</thead>', head, html, flags=re.S)


if '--no-epub' not in ARGS:
    subprocess.run(common + ['--toc', '--toc-depth=2', '--css=/data/build/book.css',
                             '--epub-metadata=/data/build/epub-metadata.xml',
                             '-o', 'secure-doc-viewer-guide.epub', WEB], check=True, env=env)
    path = os.path.join(OUT, 'secure-doc-viewer-guide.epub')
    tmp = path + '.tmp'
    with zipfile.ZipFile(path) as zin, zipfile.ZipFile(tmp, 'w') as zout:
        for item in zin.infolist():
            data = zin.read(item.filename)
            if item.filename.endswith('.xhtml'):
                data = add_scope(data.decode('utf-8')).encode('utf-8')
            elif item.filename.endswith('.opf'):
                # Pandoc's --epub-metadata import drops some of the <meta> lines (for example the accessibility
                # summary and the second accessMode), so add every line of epub-metadata.xml that is missing.
                opf = data.decode('utf-8')
                wanted = [ln.strip() for ln in open(os.path.join(HERE, 'epub-metadata.xml'), encoding='utf-8')
                          if ln.strip().startswith('<meta ')]
                extra = [ln for ln in wanted if ln not in opf]
                if extra:
                    opf = opf.replace('</metadata>', '    ' + '\n    '.join(extra) + '\n  </metadata>', 1)
                data = opf.encode('utf-8')
            zout.writestr(item, data, compress_type=zipfile.ZIP_STORED if item.filename == 'mimetype' else zipfile.ZIP_DEFLATED)
    os.replace(tmp, path)

if '--no-html' not in ARGS:
    subprocess.run(common + ['--toc', '--toc-depth=2', '--template=/data/build/templates/book.html',
                             '--css=/data/build/book.css', '--embed-resources',
                             '-o', 'secure-doc-viewer-guide.html', WEB], check=True, env=env)
    path = os.path.join(OUT, 'secure-doc-viewer-guide.html')
    html = open(path, encoding='utf-8').read()
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(add_scope(html))

if '--no-pdf' not in ARGS:
    # LuaLaTeX book, tagged PDF/UA-2: chapters start new pages, running headers, numbered sections.
    subprocess.run(common + ['--toc', '--toc-depth=1', '--metadata-file=/data/build/pdf-metadata.yaml',
                             '--pdf-engine=lualatex', '-V', 'documentclass=book', '-V', 'classoption=oneside,11pt',
                             '-V', 'papersize=a4', '-V', 'geometry:margin=2.5cm',
                             '-V', 'mainfont=texgyrepagella-regular.otf',
                             '-V', 'mainfontoptions=BoldFont=texgyrepagella-bold.otf,ItalicFont=texgyrepagella-italic.otf,BoldItalicFont=texgyrepagella-bolditalic.otf',
                             '-V', 'monofont=DejaVu Sans Mono', '-V', 'monofontoptions=Scale=0.85',
                             '-V', 'linkcolor=black', '-V', 'toccolor=black', '-V', 'urlcolor=blue!60!black',
                             '--top-level-division=chapter',
                             '--include-in-header=/data/build/header.tex', '-V', 'colorlinks=true',
                             '-o', 'secure-doc-viewer-guide.pdf', PDF], check=True, env=env)

for n in sorted(os.listdir(OUT)):
    p = os.path.join(OUT, n)
    if os.path.isfile(p):
        print(f'{os.path.getsize(p):>10}  {n}')
