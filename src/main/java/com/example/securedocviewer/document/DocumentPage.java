package com.example.securedocviewer.document;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** The tile grid of one rendered page. */
@Embeddable
public class DocumentPage {

    @Column(name = "page_index", nullable = false)
    private int pageIndex;

    @Column(name = "tile_rows", nullable = false)
    private int rows;

    @Column(name = "tile_cols", nullable = false)
    private int cols;

    @Column(name = "page_width_px", nullable = false)
    private int widthPx;

    @Column(name = "page_height_px", nullable = false)
    private int heightPx;

    protected DocumentPage() {
    }

    public DocumentPage(int pageIndex, int rows, int cols, int widthPx, int heightPx) {
        this.pageIndex = pageIndex;
        this.rows = rows;
        this.cols = cols;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getWidthPx() {
        return widthPx;
    }

    public int getHeightPx() {
        return heightPx;
    }
}
