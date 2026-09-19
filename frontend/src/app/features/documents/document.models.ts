export type Visibility = 'PRIVATE' | 'EVERYONE';

export interface PageInfo {
  page: number;
  rows: number;
  cols: number;
  tileSize: number;
  pageWidthPx: number;
  pageHeightPx: number;
}

/** A row in the document library. */
export interface DocumentSummary {
  documentId: string;
  title: string;
  pageCount: number;
  owner: string;
  visibility: Visibility;
  createdAtEpochSeconds: number;
  updatedAtEpochSeconds: number;
  canManage: boolean;
  /** Only present when canManage is true. */
  sharedWithCount: number | null;
}

/** Everything the viewer and the manage screen need. */
export interface DocumentDetail {
  documentId: string;
  title: string;
  pageCount: number;
  owner: string;
  visibility: Visibility;
  createdAtEpochSeconds: number;
  updatedAtEpochSeconds: number;
  canManage: boolean;
  /** Only present when canManage is true. */
  sharedWith: string[] | null;
  pages: PageInfo[];
  /** Bumped each time the PDF is replaced. */
  tileVersion: number;
}

export interface TileUrlGrid {
  page: number;
  rows: number;
  cols: number;
  tileSize: number;
  /** The render these URLs belong to; differs from the document's once it is replaced. */
  tileVersion: number;
  tileUrls: string[][];
}
