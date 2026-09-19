"""Builds the book as EPUB and a single HTML page with Pandoc (through Docker; nothing else to install).

Run from anywhere:   python book/build/build.py
Output:              book/build/out/secure-doc-viewer-guide.epub and .html

The manuscript is assembled first (book/build/out/manuscript.md): files are joined in the order of
order.txt with a clean break between them, line endings are normalized and the HTML comments that
carry chapter metadata and source notes are removed, because a comment directly after a list can
make Pandoc swallow the next heading.

Mermaid diagrams appear as code blocks in these outputs. Render them with a Mermaid-aware tool
(for example mdBook with mdbook-mermaid) for the final print edition.
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

common = ['docker', 'run', '--rm', '-v', BOOK.replace(BACKSLASH, '/') + ':/data', '-w', '/data/build/out',
          'pandoc/core:latest', '--from', 'gfm', '--toc', '--toc-depth=2', '--standalone',
          '--metadata', 'title=Building a Secure Document Viewer',
          '--metadata', 'subtitle=From first line of Java to production', '--metadata', 'lang=en-US']
env = dict(os.environ, MSYS_NO_PATHCONV='1')
subprocess.run(common + ['-o', 'secure-doc-viewer-guide.epub', 'manuscript.md'], check=True, env=env)
subprocess.run(common + ['--embed-resources', '-o', 'secure-doc-viewer-guide.html', 'manuscript.md'], check=True, env=env)
for n in sorted(os.listdir(OUT)):
    print(f'{os.path.getsize(os.path.join(OUT, n)):>10}  {n}')
