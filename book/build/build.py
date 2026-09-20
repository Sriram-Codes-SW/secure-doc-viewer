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
import time
import zipfile

_T = [time.time()]


def mark(label):
    """Print the seconds since the previous mark (timing information only)."""
    now = time.time()
    print(f'[timing] {label}: {now - _T[0]:.0f} s')
    _T[0] = now


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

mark('1 manuscript assembly')
# ---- 2. diagrams (cached on the diagram sources) ---------------------------------------------------
blocks = re.findall(r'```mermaid\n.*?```', manuscript, flags=re.S)
MERMAID_CONFIG = os.path.join(HERE, 'mermaid-config.json')
CONFIG_TEXT = open(MERMAID_CONFIG, encoding='utf-8').read()
CACHE = os.path.join(DIAGRAMS, 'cache')
os.makedirs(CACHE, exist_ok=True)
# Each diagram is cached under a key made from its own source AND the rendering settings, so a change to one
# diagram (or to the settings) re-renders only what changed.
keys = [hashlib.sha256((CONFIG_TEXT + b).encode('utf-8')).hexdigest()[:20] for b in blocks]
todo = [i for i, k in enumerate(keys) if not os.path.exists(os.path.join(CACHE, k + '.png'))]
if not todo:
    print(f'diagrams unchanged ({len(blocks)}), skipping the render')
else:
    # Rendering runs in small batches, each in its own headless Chrome, so the memory Chrome holds is released
    # between batches (one big run can exhaust a small machine). The pictures are the same as one run makes.
    print(f'rendering {len(todo)} of {len(blocks)} diagrams', flush=True)
    BATCH = 6
    env0 = dict(os.environ, PUPPETEER_SKIP_DOWNLOAD='1')
    work = os.path.join(DIAGRAMS, 'batch')
    for start in range(0, len(todo), BATCH):
        chunk = todo[start:start + BATCH]
        shutil.rmtree(work, ignore_errors=True)
        os.makedirs(work)
        with open(os.path.join(work, 'in.md'), 'w', encoding='utf-8', newline=chr(10)) as f:
            f.write((chr(10) * 2).join(blocks[i] for i in chunk) + chr(10))
        subprocess.run('npx --yes -p @mermaid-js/mermaid-cli mmdc -p "' + os.path.join(HERE, 'puppeteer-config.json').replace(BACKSLASH, '/')
                       + '" -c "' + MERMAID_CONFIG.replace(BACKSLASH, '/') + '" -w 4000 -i in.md -o out.md -e png -s 2 -b white',
                       cwd=work, check=True, shell=True, env=env0, stdout=subprocess.DEVNULL)
        for k, i in enumerate(chunk):
            shutil.move(os.path.join(work, f'out-{k + 1}.png'), os.path.join(CACHE, keys[i] + '.png'))
        print(f'  diagrams {start + 1}-{start + len(chunk)} of {len(todo)} rendered', flush=True)
    shutil.rmtree(work, ignore_errors=True)
# the document refers to the diagrams by position (rendered-1.png, rendered-2.png ...)
for n, k in enumerate(keys, 1):
    shutil.copyfile(os.path.join(CACHE, k + '.png'), os.path.join(DIAGRAMS, f'rendered-{n}.png'))

mark('2 diagrams (render or cache check)')
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


def wrap_code_blocks(text, width=86):
    """PDF only: break long code lines in the source text (with a continuation mark).

    LaTeX's own line breaking inside code blocks cannot be used because it is not compatible with
    tagged PDF. The web editions keep the code exactly as written.
    """
    out, in_code = [], False
    base = width
    for line in text.split('\n'):
        if line.lstrip().startswith('```'):   # fences inside list items are indented
            in_code = not in_code
            if in_code:   # a block without a language is set in a larger, unhighlighted font: wrap it sooner
                width = base if line.lstrip()[3:].strip() else 80
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

mark('3 alt text, code wrapping, writing the source files')
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
    html = re.sub(r'<thead.*?</thead>', head, html, flags=re.S)
    # Code blocks that scroll sideways must be reachable by keyboard (WCAG 2.1.1): make each one focusable.
    html = re.sub(r'<pre(?=[\s>])(?![^>]*\btabindex=)', '<pre tabindex="0"', html)
    # An <img> already has the image role; the extra role="img" Pandoc adds is redundant.
    html = re.sub(r'(<img\b[^>]*?) role="img"', r'\1', html)
    return html


mark('4 docker image check and metadata')
if '--no-epub' not in ARGS:
    subprocess.run(common + ['--toc', '--toc-depth=2', '--css=/data/build/book.css',
                             '--epub-metadata=/data/build/epub-metadata.xml',
                             '--epub-cover-image=/data/build/cover.png',
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

mark('5 EPUB (pandoc and post-processing)')
if '--no-html' not in ARGS:
    subprocess.run(common + ['--toc', '--toc-depth=2', '--template=/data/build/templates/book.html',
                             '--css=/data/build/book.css', '--embed-resources',
                             '-o', 'secure-doc-viewer-guide.html', WEB], check=True, env=env)
    path = os.path.join(OUT, 'secure-doc-viewer-guide.html')
    html = open(path, encoding='utf-8').read()
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(add_scope(html))

mark('6 HTML (pandoc and post-processing)')
if '--no-pdf' not in ARGS:
    # LuaLaTeX book, tagged PDF/UA-2: chapters start new pages, running headers, numbered sections.
    pdf_cmd = common + ['--toc', '--toc-depth=1', '--metadata-file=/data/build/pdf-metadata.yaml',
                             '--pdf-engine=lualatex', '-V', 'documentclass=book', '-V', 'classoption=twoside,openany,11pt',
                             '-V', 'papersize=letter', '-V', 'geometry:inner=1in', '-V', 'geometry:outer=1.2in',
                             '-V', 'geometry:top=1.05in', '-V', 'geometry:bottom=1.05in',
                             '-V', 'geometry:headheight=14pt', '-V', 'geometry:headsep=16pt',
                             '-V', 'mainfont=texgyrepagella-regular.otf',
                             '-V', 'mainfontoptions=BoldFont=texgyrepagella-bold.otf,ItalicFont=texgyrepagella-italic.otf,BoldItalicFont=texgyrepagella-bolditalic.otf',
                             '-V', 'monofont=DejaVu Sans Mono', '-V', 'monofontoptions=Scale=0.86',
                             '-V', 'linkcolor=black', '-V', 'toccolor=black', '-V', 'urlcolor=blue!60!black',
                             '--top-level-division=chapter',
                             '--include-in-header=/data/build/header.tex', '-V', 'colorlinks=true',
                             '-o', 'secure-doc-viewer-guide.pdf', PDF]
    if os.environ.get('SDV_PROFILE'):
        # Profiling only (set SDV_PROFILE=1): the same command plus --verbose, with the seconds at which each
        # Pandoc/LaTeX step is reported. The output is identical; only the messages differ.
        t0 = time.time()
        proc = subprocess.Popen(pdf_cmd + ['--verbose'], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True,
                                encoding='utf-8', errors='replace', env=env)
        for msg in proc.stdout:
            if msg.startswith('[') and any(k in msg for k in ('Running', 'rerun', 'Rerun', 'LaTeX', 'lualatex', 'PDF', 'Loading', 'template')):
                print(f'[timing] {time.time() - t0:6.0f} s  {msg.strip()[:150]}')
        if proc.wait() != 0:
            raise subprocess.CalledProcessError(proc.returncode, pdf_cmd)
    else:
        subprocess.run(pdf_cmd, check=True, env=env)

mark('7 PDF (pandoc and LuaLaTeX)')
for n in sorted(os.listdir(OUT)):
    p = os.path.join(OUT, n)
    if os.path.isfile(p):
        print(f'{os.path.getsize(p):>10}  {n}')
