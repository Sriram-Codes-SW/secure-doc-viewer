import { PageInfo, TileUrlGrid } from '../documents/document.models';

export interface TileViewModel {
  /** Stable per grid cell ("row-col"), unlike url which changes on every re-issue. */
  key: string;
  url: string;
  top: number;
  left: number;
  width: number;
  height: number;
}

/**
 * Turns a signed-URL grid into absolutely-positioned tile rectangles. Edge
 * tiles are cropped shorter/narrower than a full tile whenever the page
 * dimensions aren't an exact multiple of tileSize, so each tile's actual
 * width/height has to be derived from the page's pixel dimensions rather
 * than assumed to equal tileSize.
 */
export function buildTileViewModels(grid: TileUrlGrid, pageInfo: PageInfo, apiBaseUrl: string): TileViewModel[] {
  const tiles: TileViewModel[] = [];
  for (let row = 0; row < grid.rows; row++) {
    for (let col = 0; col < grid.cols; col++) {
      const left = col * grid.tileSize;
      const top = row * grid.tileSize;
      const width = Math.min(grid.tileSize, pageInfo.pageWidthPx - left);
      const height = Math.min(grid.tileSize, pageInfo.pageHeightPx - top);
      tiles.push({
        key: `${row}-${col}`,
        url: `${apiBaseUrl}${grid.tileUrls[row][col]}`,
        top,
        left,
        width,
        height,
      });
    }
  }
  return tiles;
}
