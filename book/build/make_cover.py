"""Draws the EPUB cover (cover.png): text and a simple tile motif, no stock art, no logos.

The motif is the idea of the book: a page cut into tiles, with one tile picked out.
Usage: python book/build/make_cover.py     (needs pymupdf; writes book/build/cover.png)
The text is white on navy (contrast about 11:1). The cover is the only place where text is part of an image;
the title, subtitle and author are also in the EPUB metadata, so nothing depends on reading the picture.
"""
import os

import pymupdf

HERE = os.path.dirname(os.path.abspath(__file__))
W, H = 1600, 2560


def rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) / 255 for i in (0, 2, 4))


NAVY = rgb('1F3D5C')
TILES = [rgb(c) for c in ('27496F', '30577F', '3B6790', '2B4F77', '4A78A3')]
PICKED = rgb('E9EEF4')
WHITE = rgb('FFFFFF')
SOFT = rgb('D6E0EC')

doc = pymupdf.open()
page = doc.new_page(width=W, height=H)
page.draw_rect(pymupdf.Rect(0, 0, W, H), color=None, fill=NAVY)

size, gap, cols, rows = 150, 12, 6, 8
x0, y0 = 160, 170
for r in range(rows):
    for c in range(cols):
        k = (r * 5 + c * 3) % 5
        colour = PICKED if (r, c) == (3, 2) else TILES[k]
        x, y = x0 + c * (size + gap), y0 + r * (size + gap)
        page.draw_rect(pymupdf.Rect(x, y, x + size, y + size), color=None, fill=colour)

# The title block sits under the tiles; Helvetica (built in) keeps the file free of font licences.
y = y0 + rows * (size + gap) + 200
for line in ('Building a', 'Secure', 'Document Viewer'):
    page.insert_text((x0, y), line, fontname='hebo', fontsize=150, color=WHITE)
    y += 168
page.insert_text((x0, y + 30), 'From First Line of Java to Production', fontname='helv', fontsize=62, color=SOFT)
page.insert_text((x0, H - 150), 'Claude (Anthropic)', fontname='hebo', fontsize=58, color=WHITE)

pix = page.get_pixmap(dpi=72)
out = os.path.join(HERE, 'cover.png')
pix.save(out)
print('wrote', out, pix.width, 'x', pix.height)
