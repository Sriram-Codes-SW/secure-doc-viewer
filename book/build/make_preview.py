"""Draws the social preview card: 1280x640 (GitHub) and 1200x630 (LinkedIn / Open Graph).
Same look as the book cover: navy, a page cut into tiles with one tile picked out, white text (contrast about 11:1).
Built-in Helvetica keeps the files free of font licences. usage: python make_preview.py [output-dir]
"""
import os
import sys

import pymupdf
from PIL import Image

OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.abspath(__file__))
W, H = 1280, 640


def rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) / 255 for i in (0, 2, 4))


NAVY, WHITE, SOFT, PICKED = rgb('1F3D5C'), rgb('FFFFFF'), rgb('D6E0EC'), rgb('E9EEF4')
TILES = [rgb(c) for c in ('27496F', '30577F', '3B6790', '2B4F77', '4A78A3')]

doc = pymupdf.open()
page = doc.new_page(width=W, height=H)
page.draw_rect(pymupdf.Rect(0, 0, W, H), color=None, fill=NAVY)

# tile motif (left): 5 columns x 6 rows
size, gap, cols, rows = 64, 8, 5, 6
gw, gh = cols * size + (cols - 1) * gap, rows * size + (rows - 1) * gap
x0, y0 = 84, (H - gh) // 2
for r in range(rows):
    for c in range(cols):
        colour = PICKED if (r, c) == (2, 2) else TILES[(r * 5 + c * 3) % 5]
        x, y = x0 + c * (size + gap), y0 + r * (size + gap)
        page.draw_rect(pymupdf.Rect(x, y, x + size, y + size), color=None, fill=colour)

# text (right), each line shrunk if needed so it fits inside the safe area
tx = x0 + gw + 76
avail = W - tx - 70


def put(text, y, font, size, colour):
    while pymupdf.get_text_length(text, fontname=font, fontsize=size) > avail:
        size -= 1
    page.insert_text((tx, y), text, fontname=font, fontsize=size, color=colour)
    return size


put('Building a Secure', 236, 'hebo', 74, WHITE)
put('Document Viewer', 318, 'hebo', 74, WHITE)
put('From first line of Java to production', 376, 'helv', 33, SOFT)
put('A free textbook: 41 chapters, 242 exercises', 448, 'helv', 27, WHITE)
put('81 diagrams, glossary and index, PDF, EPUB and web', 486, 'helv', 27, WHITE)
put('By Claude (Anthropic)  |  Spring Boot, Angular, Docker', 566, 'hebo', 24, SOFT)

pix = page.get_pixmap(dpi=72)
p1 = os.path.join(OUT, 'preview-1280x640.png')
pix.save(p1)
# 1200x630 for LinkedIn and Open Graph: scale to 1200x600 and centre on navy
img = Image.open(p1).convert('RGB').resize((1200, 600), Image.LANCZOS)
canvas = Image.new('RGB', (1200, 630), (0x1F, 0x3D, 0x5C))
canvas.paste(img, (0, 15))
p2 = os.path.join(OUT, 'preview-1200x630.png')
canvas.save(p2, optimize=True)
print('wrote', p1, pix.width, 'x', pix.height, '|', p2, os.path.getsize(p1) // 1024, 'KB and', os.path.getsize(p2) // 1024, 'KB')
