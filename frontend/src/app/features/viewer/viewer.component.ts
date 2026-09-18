import { HttpErrorResponse } from '@angular/common/http';
import { DecimalPipe, NgStyle } from '@angular/common';
import { Component, HostListener, OnDestroy, OnInit, computed, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { API_BASE_URL } from '../../core/config';
import { SessionService } from '../../core/session.service';
import { DocumentsService } from '../documents/documents.service';
import { DocumentDetail } from '../documents/document.models';
import { buildTileViewModels, TileViewModel } from './tile-view-model';

const MAX_ZOOM = 2;
const MIN_ZOOM = 0.4;
const FIT_WIDTH_PX = 900;

/**
 * Tiles are fetched here rather than handed to the browser as CSS image
 * URLs, because only fetch() exposes the response status: a throttled (429)
 * or expired (401) tile is otherwise indistinguishable from a blank one, and
 * the page silently renders with holes. A small worker pool keeps a page
 * change from leaving dozens of now-irrelevant requests in flight, each of
 * which would still spend the session's rate-limit budget.
 */
const MAX_CONCURRENT_TILE_FETCHES = 6;
/** Used only if a 429 arrives without a usable Retry-After header. */
const FALLBACK_RETRY_AFTER_SECONDS = 5;

type TileStatus = 'pending' | 'loaded' | 'failed';

/** Per-user, per-document "where was I" — a convenience only, so failures to read/write are ignored. */
const LAST_PAGE_KEY_PREFIX = 'sdv.lastPage.';

interface TileState extends TileViewModel {
  /** Object URL of the fetched PNG, once loaded. */
  src: string | null;
  status: TileStatus;
}

@Component({
  selector: 'app-viewer',
  standalone: true,
  imports: [RouterLink, NgStyle, DecimalPipe],
  templateUrl: './viewer.component.html',
  styleUrl: './viewer.component.css',
})
export class ViewerComponent implements OnInit, OnDestroy {
  private documentId = '';
  /** Bumped on every page change so late responses for an old page are dropped. */
  private loadGeneration = 0;
  private abortController = new AbortController();
  private gridSub: Subscription | null = null;
  private retryTimer: ReturnType<typeof setTimeout> | null = null;
  private countdownTimer: ReturnType<typeof setInterval> | null = null;

  readonly manifest = signal<DocumentDetail | null>(null);
  readonly currentPage = signal(0);
  readonly tiles = signal<TileState[]>([]);
  readonly zoom = signal(1);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly pageInputError = signal<string | null>(null);
  /** Seconds until throttled tiles are retried; null when not throttled. */
  readonly throttledSeconds = signal<number | null>(null);

  readonly pageInfo = computed(() => this.manifest()?.pages[this.currentPage()] ?? null);
  readonly loadedTileCount = computed(() => this.tiles().filter((t) => t.status === 'loaded').length);
  readonly hasFailedTiles = computed(() => this.tiles().some((t) => t.status === 'failed'));

  readonly stageStyle = computed(() => {
    const info = this.pageInfo();
    if (!info) {
      return {};
    }
    const fitScale = Math.min(1, FIT_WIDTH_PX / info.pageWidthPx);
    const scale = fitScale * this.zoom();
    return {
      width: `${info.pageWidthPx}px`,
      height: `${info.pageHeightPx}px`,
      transform: `scale(${scale})`,
      'transform-origin': 'top left',
    };
  });

  readonly stageWrapperStyle = computed(() => {
    const info = this.pageInfo();
    if (!info) {
      return {};
    }
    const fitScale = Math.min(1, FIT_WIDTH_PX / info.pageWidthPx);
    const scale = fitScale * this.zoom();
    return {
      width: `${info.pageWidthPx * scale}px`,
      height: `${info.pageHeightPx * scale}px`,
    };
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly documentsService: DocumentsService,
    private readonly sessionService: SessionService,
  ) {}

  ngOnInit(): void {
    this.documentId = this.route.snapshot.paramMap.get('documentId') ?? '';
    this.documentsService.get(this.documentId).subscribe({
      next: (manifest) => {
        this.manifest.set(manifest);
        this.loadPage(this.initialPage(manifest.pageCount));
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage.set(
          err.status === 404
            ? 'This document doesn’t exist, or it hasn’t been shared with you.'
            : 'Could not load this document.',
        );
        this.loading.set(false);
      },
    });
  }

  ngOnDestroy(): void {
    this.resetPageState();
  }

  loadPage(page: number): void {
    const manifest = this.manifest();
    if (!manifest || page < 0 || page >= manifest.pageCount) {
      return;
    }
    this.resetPageState();
    this.pageInputError.set(null);
    this.errorMessage.set(null);
    this.currentPage.set(page);
    this.loading.set(true);
    this.rememberPage(page);
    this.requestGrid(page, this.loadGeneration, true);
  }

  /**
   * Keyboard navigation, ignored while typing in a field or with modifier
   * keys held (so browser shortcuts like Ctrl+Plus still work).
   */
  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    const target = event.target as HTMLElement | null;
    if (event.ctrlKey || event.metaKey || event.altKey || !this.manifest()
        || (target && /^(INPUT|TEXTAREA|SELECT)$/.test(target.tagName)) || target?.isContentEditable) {
      return;
    }
    const last = this.manifest()!.pageCount - 1;
    const actions: Record<string, () => void> = {
      ArrowRight: () => this.nextPage(),
      PageDown: () => this.nextPage(),
      ArrowLeft: () => this.prevPage(),
      PageUp: () => this.prevPage(),
      Home: () => this.currentPage() !== 0 && this.loadPage(0),
      End: () => this.currentPage() !== last && this.loadPage(last),
      '+': () => this.zoomIn(),
      '=': () => this.zoomIn(),
      '-': () => this.zoomOut(),
    };
    const action = actions[event.key];
    if (action) {
      event.preventDefault();
      action();
    }
  }

  /** ?page=N (1-based) wins; otherwise resume where this user left off; otherwise page 1. */
  private initialPage(pageCount: number): number {
    const requested = Number(this.route.snapshot.queryParamMap.get('page'));
    if (Number.isInteger(requested) && requested >= 1 && requested <= pageCount) {
      return requested - 1;
    }
    try {
      const saved = Number(localStorage.getItem(this.lastPageKey()));
      return Number.isInteger(saved) && saved >= 0 && saved < pageCount ? saved : 0;
    } catch {
      return 0;
    }
  }

  /** Keeps the URL shareable (?page=N) and remembers the page for next time. */
  private rememberPage(page: number): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: page + 1 },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
    try {
      localStorage.setItem(this.lastPageKey(), String(page));
    } catch {
      // Storage unavailable (private mode, quota); resuming is just a convenience.
    }
  }

  private lastPageKey(): string {
    return `${LAST_PAGE_KEY_PREFIX}${this.sessionService.username() ?? ''}.${this.documentId}`;
  }

  /**
   * Jump straight to a page typed by the user (1-based). Goes through the
   * same loadPage path as Prev/Next, so signed URLs, watermarking and the
   * rate limit apply identically — there is no shortcut route to a page.
   */
  goToPage(raw: string): void {
    const pageCount = this.manifest()?.pageCount ?? 0;
    const pageNumber = Number(raw);
    if (!raw.trim() || !Number.isInteger(pageNumber) || pageNumber < 1 || pageNumber > pageCount) {
      this.pageInputError.set(`Enter a whole number from 1 to ${pageCount}.`);
      return;
    }
    this.pageInputError.set(null);
    if (pageNumber - 1 !== this.currentPage()) {
      this.loadPage(pageNumber - 1);
    }
  }

  retryFailedTiles(): void {
    this.errorMessage.set(null);
    this.tiles.update((tiles) => tiles.map((t) => (t.status === 'failed' ? { ...t, status: 'pending' } : t)));
    this.requestGrid(this.currentPage(), this.loadGeneration, true);
  }

  nextPage(): void {
    this.loadPage(this.currentPage() + 1);
  }

  prevPage(): void {
    this.loadPage(this.currentPage() - 1);
  }

  zoomIn(): void {
    this.zoom.update((z) => Math.min(MAX_ZOOM, +(z + 0.2).toFixed(2)));
  }

  zoomOut(): void {
    this.zoom.update((z) => Math.max(MIN_ZOOM, +(z - 0.2).toFixed(2)));
  }

  /**
   * Cosmetic only — see the "does this actually block download" discussion:
   * this stops the casual right-click path, nothing more. The real
   * protection is server-side (short-lived signed URLs, per-request
   * watermarking, rate limiting), which stays effective even with this
   * handler removed entirely.
   */
  blockContextMenu(event: MouseEvent): void {
    event.preventDefault();
  }

  /**
   * Fetches a fresh signed-URL grid for the page. Tiles already loaded keep
   * their pixels; only the rest pick up the new URLs. Loaded tiles never
   * need re-issuing, so there is no periodic refresh spending rate-limit
   * budget on a page that is just sitting open.
   */
  private requestGrid(page: number, generation: number, allowUrlReissue: boolean): void {
    const info = this.manifest()?.pages[page];
    if (!info) {
      return;
    }
    this.gridSub?.unsubscribe();
    this.gridSub = this.documentsService.getTileUrls(this.documentId, page).subscribe({
      next: (grid) => {
        if (generation !== this.loadGeneration) {
          return;
        }
        const existing = new Map(this.tiles().map((t) => [t.key, t]));
        this.tiles.set(
          buildTileViewModels(grid, info, API_BASE_URL).map((fresh) => {
            const previous = existing.get(fresh.key);
            return previous?.status === 'loaded' ? previous : { ...fresh, src: null, status: 'pending' as const };
          }),
        );
        this.loading.set(false);
        void this.fetchPendingTiles(page, generation, allowUrlReissue);
      },
      error: () => {
        if (generation !== this.loadGeneration) {
          return;
        }
        this.errorMessage.set('Could not load this page.');
        this.loading.set(false);
      },
    });
  }

  private async fetchPendingTiles(page: number, generation: number, allowUrlReissue: boolean): Promise<void> {
    const queue = this.tiles().filter((t) => t.status !== 'loaded');
    const { signal } = this.abortController;
    const outcome = { retryAfterSeconds: null as number | null, unauthorized: false, accessRevoked: false };

    const worker = async (): Promise<void> => {
      // Once any request is throttled, stop issuing new ones: every further
      // request in this window would be rejected anyway.
      while (queue.length > 0 && outcome.retryAfterSeconds === null) {
        const tile = queue.shift()!;
        let response: Response;
        try {
          response = await fetch(tile.url, { signal, cache: 'no-store' });
        } catch {
          if (signal.aborted) {
            return;
          }
          this.updateTile(tile.key, { status: 'failed' });
          continue;
        }
        if (generation !== this.loadGeneration) {
          return;
        }
        if (response.ok) {
          const blob = await response.blob();
          if (generation !== this.loadGeneration) {
            return;
          }
          this.updateTile(tile.key, { status: 'loaded', src: URL.createObjectURL(blob) });
        } else if (response.status === 429) {
          outcome.retryAfterSeconds = parseRetryAfter(response.headers.get('Retry-After'));
        } else if (response.status === 401) {
          outcome.unauthorized = true;
        } else if (response.status === 404) {
          // Unshared or deleted while open: access is re-checked on every tile.
          outcome.accessRevoked = true;
        } else {
          this.updateTile(tile.key, { status: 'failed' });
        }
      }
    };

    await Promise.all(Array.from({ length: MAX_CONCURRENT_TILE_FETCHES }, () => worker()));
    if (generation !== this.loadGeneration || signal.aborted) {
      return;
    }

    if (outcome.accessRevoked) {
      this.errorMessage.set('You no longer have access to this document.');
    } else if (outcome.retryAfterSeconds !== null) {
      this.startThrottleCountdown(page, generation, outcome.retryAfterSeconds);
    } else if (outcome.unauthorized && allowUrlReissue) {
      // A tile URL expired before we reached it, or the session was
      // revoked. Re-issue once: if the session is gone, the grid request
      // itself 401s and the session interceptor signs the user out.
      this.requestGrid(page, generation, false);
    } else if (outcome.unauthorized || this.hasFailedTiles()) {
      this.tiles.update((tiles) => tiles.map((t) => (t.status === 'pending' ? { ...t, status: 'failed' } : t)));
      this.errorMessage.set('Some parts of this page could not be loaded.');
    }
  }

  private startThrottleCountdown(page: number, generation: number, seconds: number): void {
    this.clearThrottle();
    this.throttledSeconds.set(seconds);
    this.countdownTimer = setInterval(
      () => this.throttledSeconds.update((s) => (s === null ? null : Math.max(0, s - 1))),
      1000,
    );
    this.retryTimer = setTimeout(() => {
      this.clearThrottle();
      if (generation === this.loadGeneration) {
        // Fresh URLs, not the old ones: waiting out the window can take
        // long enough for the original tokens to have expired.
        this.requestGrid(page, generation, true);
      }
    }, seconds * 1000);
  }

  private clearThrottle(): void {
    if (this.retryTimer !== null) {
      clearTimeout(this.retryTimer);
      this.retryTimer = null;
    }
    if (this.countdownTimer !== null) {
      clearInterval(this.countdownTimer);
      this.countdownTimer = null;
    }
    this.throttledSeconds.set(null);
  }

  private resetPageState(): void {
    this.loadGeneration++;
    this.abortController.abort();
    this.abortController = new AbortController();
    this.gridSub?.unsubscribe();
    this.clearThrottle();
    for (const tile of this.tiles()) {
      if (tile.src) {
        URL.revokeObjectURL(tile.src);
      }
    }
    this.tiles.set([]);
  }

  private updateTile(key: string, patch: Partial<TileState>): void {
    this.tiles.update((tiles) => tiles.map((t) => (t.key === key ? { ...t, ...patch } : t)));
  }
}

function parseRetryAfter(header: string | null): number {
  const seconds = Number(header);
  return Number.isFinite(seconds) && seconds > 0 ? Math.ceil(seconds) : FALLBACK_RETRY_AFTER_SECONDS;
}
