"""Builds the book as EPUB and a single HTML page with Pandoc (through Docker; nothing else to install).

Run from anywhere:   python book/build/build.py
Output:              book/build/out/secure-doc-viewer-guide.epub and .html

The manuscript is assembled first (book/build/out/manuscript.md): files are joined in the order of
order.txt with a clean break between them, line endings are normalized and the HTML comments that
carry chapter metadata and source notes are removed, because a comment directly after a list can
make Pandoc swallow the next heading.

Mermaid diagrams are rendered to PNG first (mermaid-cli + host Chrome) and used by every format.
The PDF is typeset with XeLaTeX as a book (header.tex holds the layout).
"""
import os
import re
import subprocess

HERE = os.path.dirname(os.path.abspath(__file__))
BOOK = os.path.dirname(HERE)
OUT = os.path.join(HERE, 'out')
os.makedirs(OUT, exist_ok=True)

BACKSLASH = chr(92)
parts = []
for name in open(os.path.join(HERE, 'order.txt'), encoding='utf-8').read().split():
    text = open(os.path.join(BOOK, name), encoding='utf-8').read().replace('\r\n', '\n')
    text = re.sub(r'<!--.*?-->[ \t]*\n?', '', text, flags=re.S)
    parts.append(text.strip('\n'))
with open(os.path.join(OUT, 'manuscript.md'), 'w', encoding='utf-8', newline='\n') as f:
    f.write('\n\n'.join(parts) + '\n')

# Render Mermaid diagrams to PNG images (host Chrome through puppeteer-config.json), then build from rendered.md.
os.makedirs(os.path.join(OUT, 'diagrams'), exist_ok=True)
env0 = dict(os.environ, PUPPETEER_SKIP_DOWNLOAD='1')
subprocess.run('npx --yes -p @mermaid-js/mermaid-cli mmdc -p ../puppeteer-config.json -i manuscript.md '
               '-o diagrams/rendered.md -e png -s 2 -b white', cwd=OUT, check=True, shell=True, env=env0)
SRC = 'diagrams/rendered.md'

# Accessibility: give each rendered diagram the text of its italic caption (the line below it) as alt text.
rp = os.path.join(OUT, SRC)
lines = open(rp, encoding='utf-8').read().split('\n')
for i, line in enumerate(lines):
    m = re.match(r'!\[diagram\]\((.+?)\)$', line)
    if not m:
        continue
    alt = 'Diagram'
    for nxt in lines[i + 1:i + 4]:
        if nxt.strip():
            cm = re.match(r'\*(Figure [^*]+)\*\s*$', nxt.strip())
            if cm:
                alt = 'Diagram. ' + cm.group(1).replace('[', '(').replace(']', ')')
            break
    lines[i] = f'![{alt}]({m.group(1)})'
with open(rp, 'w', encoding='utf-8', newline='\n') as f:
    f.write('\n'.join(lines))

# PDF only: no font in the image has colour emoji, so print the one emoji (in a Java string) as its escape.
pdf_text = open(rp, encoding='utf-8').read()
pdf_text = pdf_text.replace('\U0001F600', BACKSLASH + 'uD83D' + BACKSLASH + 'uDE00')
with open(os.path.join(OUT, 'diagrams', 'rendered-pdf.md'), 'w', encoding='utf-8', newline='\n') as f:
    f.write(pdf_text)

# Pandoc + XeLaTeX image with real fonts (see Dockerfile); built once, cached afterwards.
subprocess.run(['docker', 'build', '-q', '-t', 'sdv-book-pandoc', HERE], check=True)
common = ['docker', 'run', '--rm', '-v', BOOK.replace(BACKSLASH, '/') + ':/data', '-w', '/data/build/out',
          'sdv-book-pandoc', '--from', 'gfm', '--toc', '--toc-depth=2', '--standalone', '--resource-path=.:/data/build/out/diagrams',
          '--metadata', 'title=Building a Secure Document Viewer',
          '--metadata', 'subtitle=From first line of Java to production', '--metadata', 'lang=en-US']
env = dict(os.environ, MSYS_NO_PATHCONV='1')
subprocess.run(common + ['-o', 'secure-doc-viewer-guide.epub', SRC], check=True, env=env)
subprocess.run(common + ['--embed-resources', '-o', 'secure-doc-viewer-guide.html', SRC], check=True, env=env)
# LaTeX book PDF (XeLaTeX): chapters start new pages, running headers, wrapped code, numbered sections.
subprocess.run(common + ['--pdf-engine=xelatex', '-V', 'documentclass=book', '-V', 'classoption=oneside,11pt', '-V', 'papersize=a4',
               '-V', 'geometry:margin=2.5cm', '-V', 'mainfont=texgyrepagella-regular.otf',
               '-V', 'mainfontoptions=BoldFont=texgyrepagella-bold.otf,ItalicFont=texgyrepagella-italic.otf,BoldItalicFont=texgyrepagella-bolditalic.otf',
               '-V', 'monofont=DejaVu Sans Mono', '-V', 'monofontoptions=Scale=0.85', '--top-level-division=chapter', '--syntax-highlighting=tango',
               '--include-in-header=/data/build/header.tex', '-V', 'colorlinks=true', '-o', 'secure-doc-viewer-guide.pdf', 'diagrams/rendered-pdf.md'],
               check=True, env=env)
for n in sorted(os.listdir(OUT)):
    print(f'{os.path.getsize(os.path.join(OUT, n)):>10}  {n}')
