export interface PageInfo {
  page: number;
  rows: number;
  cols: number;
  tileSize: number;
  pageWidthPx: number;
  pageHeightPx: number;
}

export interface DocumentManifest {
  documentId: string;
  title: string;
  pageCount: number;
  pages: PageInfo[];
}

export interface TileUrlGrid {
  page: number;
  rows: number;
  cols: number;
  tileSize: number;
  tileUrls: string[][];
}
