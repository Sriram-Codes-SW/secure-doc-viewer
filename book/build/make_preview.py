"""Draws the two preview cards.
- preview-1280x640.png: GitHub social preview (Settings > General > Social preview). Same look as the book cover: navy, a page
  cut into tiles with one tile picked out, white text (contrast about 11:1).
- preview-link-2400x1260.png: the card for links shared on LinkedIn and other sites (Open Graph, 1.91:1). It is drawn at twice
  the size with a larger title and fewer words, because those sites show it as a small thumbnail.
Built-in Helvetica keeps the files free of font licences. usage: python make_preview.py [output-dir]
"""
import os
import sys

import pymupdf

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
print('wrote', p1, pix.width, 'x', pix.height, os.path.getsize(p1) // 1024, 'KB')

# ---- link card: 1200x630 layout drawn at 2x (2400x1260), larger text, tile row along the bottom ----
LW, LH = 1200, 630
LSOFT = rgb('E3EBF4')
ldoc = pymupdf.open()
lp = ldoc.new_page(width=LW, height=LH)
lp.draw_rect(pymupdf.Rect(0, 0, LW, LH), color=None, fill=NAVY)
ts, tg, tn, ty = 56, 8, 15, 548
for c in range(tn):
    colour = PICKED if c == 7 else TILES[(c * 3) % 5]
    x = 60 + c * (ts + tg)
    lp.draw_rect(pymupdf.Rect(x, ty, x + ts, ty + ts), color=None, fill=colour)
lavail = LW - 120


def lput(text, y, font, size, colour):
    while pymupdf.get_text_length(text, fontname=font, fontsize=size) > lavail:
        size -= 1
    lp.insert_text((60, y), text, fontname=font, fontsize=size, color=colour)


lput('Building a Secure', 135, 'hebo', 112, WHITE)
lput('Document Viewer', 250, 'hebo', 112, WHITE)
lput('A free textbook: from first line of', 322, 'hebo', 46, LSOFT)
lput('Java to production', 376, 'hebo', 46, LSOFT)
lput('41 chapters | 242 exercises | 81 diagrams', 444, 'hebo', 40, WHITE)
lput('By Claude (Anthropic)', 500, 'helv', 34, LSOFT)
p2 = os.path.join(OUT, 'preview-link-2400x1260.png')
lpix = lp.get_pixmap(dpi=144)
lpix.save(p2)
print('wrote', p2, lpix.width, 'x', lpix.height, os.path.getsize(p2) // 1024, 'KB')
