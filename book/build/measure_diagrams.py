"""Prints how large the text in each diagram of a chapter file will be on the printed page.

usage:  python book/build/measure_diagrams.py <chapter.md> [<chapter2.md> ...]

Each Mermaid diagram is rendered with the book's own settings (mermaid-config.json, 4000 px window, scale 2)
into a scratch folder, and the printed text size is estimated as
    font size in the image (26 px x 2)  x  (text width in pixels-to-points)  =  52 x 453 / image width
for the current A4 text width (453 pt); tall diagrams are limited by the page height. The book's target is
at least 7 pt (10 pt is comfortable). A diagram that is too small should be made narrower: shorter labels,
top-to-bottom layout, or two rows instead of one long chain.
"""
import os
import re
import shutil
import subprocess
import sys
import tempfile

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
TEXT_WIDTH_PT = 453.0
TEXT_HEIGHT_PT = 0.8 * 690
FONT_PX = 26 * 2


def main(paths):
    cfg = os.path.join(HERE, 'mermaid-config.json').replace(chr(92), '/')
    pup = os.path.join(HERE, 'puppeteer-config.json').replace(chr(92), '/')
    for path in paths:
        text = open(path, encoding='utf-8').read().replace('\r\n', '\n')
        blocks = list(re.finditer(r'```mermaid\n.*?```', text, flags=re.S))
        if not blocks:
            print(f'{path}: no diagrams')
            continue
        tmp = tempfile.mkdtemp(prefix='sdv-measure-')
        try:
            src = os.path.join(tmp, 'in.md')
            with open(src, 'w', encoding='utf-8', newline='\n') as f:
                f.write(text)
            env = dict(os.environ, PUPPETEER_SKIP_DOWNLOAD='1')
            subprocess.run(f'npx --yes -p @mermaid-js/mermaid-cli mmdc -p "{pup}" -c "{cfg}" -w 4000 -i in.md '
                           '-o out.md -e png -s 2 -b white', cwd=tmp, check=True, shell=True, env=env,
                           stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            print(os.path.basename(path))
            for i, m in enumerate(blocks, 1):
                w, h = Image.open(os.path.join(tmp, f'out-{i}.png')).size
                cap = re.search(r'\*(Figure [^*\n]+)\*', text[m.end():m.end() + 400])
                pt = FONT_PX * min(TEXT_WIDTH_PT / w, TEXT_HEIGHT_PT / (h / 1.0))
                flag = 'OK ' if pt >= 7 else 'SMALL'
                print(f'  {flag} {pt:5.1f} pt  {w}x{h}px  {cap.group(1)[:60] if cap else "(no caption)"}')
        finally:
            shutil.rmtree(tmp, ignore_errors=True)


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    main(sys.argv[1:])
