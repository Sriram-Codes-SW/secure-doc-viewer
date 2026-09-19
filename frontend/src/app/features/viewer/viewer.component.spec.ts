import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { DocumentDetail } from '../documents/document.models';
import { ViewerComponent } from './viewer.component';

const DOC: DocumentDetail = {
  documentId: 'doc-1',
  title: 'Doc',
  pageCount: 5,
  owner: 'owner',
  visibility: 'EVERYONE',
  createdAtEpochSeconds: 0,
  updatedAtEpochSeconds: 0,
  canManage: false,
  sharedWith: null,
  pages: Array.from({ length: 5 }, (_, page) => ({
    page, rows: 1, cols: 1, tileSize: 256, pageWidthPx: 200, pageHeightPx: 200,
  })),
  tileVersion: 1,
};

describe('ViewerComponent navigation', () => {
  let http: HttpTestingController;

  function create(queryParams: Record<string, string> = {}): ViewerComponent {
    TestBed.configureTestingModule({
      imports: [ViewerComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ documentId: 'doc-1' }),
              queryParamMap: convertToParamMap(queryParams),
            },
          },
        },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    const component = TestBed.createComponent(ViewerComponent).componentInstance;
    component.ngOnInit();
    http.expectOne('/api/documents/doc-1').flush(DOC);
    return component;
  }

  function expectGridRequestFor(page: number): void {
    http.expectOne(`/api/documents/doc-1/pages/${page}/tile-urls`);
  }

  beforeEach(() => localStorage.clear());

  it('opens the page named in ?page= (1-based)', () => {
    const viewer = create({ page: '3' });
    expect(viewer.currentPage()).toBe(2);
    expectGridRequestFor(2);
  });

  it('ignores an out-of-range ?page= and resumes the last page read instead', () => {
    localStorage.setItem('sdv.lastPage..doc-1', '4');
    const viewer = create({ page: '99' });
    expect(viewer.currentPage()).toBe(4);
    expectGridRequestFor(4);
  });

  it('turns pages with the arrow keys and jumps with Home/End', () => {
    const viewer = create();
    expectGridRequestFor(0);

    viewer.onKeydown(new KeyboardEvent('keydown', { key: 'ArrowRight' }));
    expect(viewer.currentPage()).toBe(1);
    expectGridRequestFor(1);

    viewer.onKeydown(new KeyboardEvent('keydown', { key: 'End' }));
    expect(viewer.currentPage()).toBe(4);
    expectGridRequestFor(4);

    viewer.onKeydown(new KeyboardEvent('keydown', { key: 'Home' }));
    expect(viewer.currentPage()).toBe(0);
    expectGridRequestFor(0);
  });

  it('leaves keys alone while typing in a field or with modifiers held', () => {
    const viewer = create();
    expectGridRequestFor(0);

    const input = document.createElement('input');
    const typing = new KeyboardEvent('keydown', { key: 'ArrowRight' });
    Object.defineProperty(typing, 'target', { value: input });
    viewer.onKeydown(typing);
    viewer.onKeydown(new KeyboardEvent('keydown', { key: 'ArrowRight', ctrlKey: true }));

    expect(viewer.currentPage()).toBe(0);
    http.verify();
  });

  function touch(x: number, y: number): Touch {
    return { clientX: x, clientY: y } as Touch;
  }

  it('turns pages with a horizontal swipe, but not when zoomed in (then a swipe pans)', () => {
    const viewer = create();
    expectGridRequestFor(0);

    viewer.onTouchStart({ touches: [touch(300, 200)] } as unknown as TouchEvent);
    viewer.onTouchEnd({ changedTouches: [touch(150, 210)] } as unknown as TouchEvent);
    expect(viewer.currentPage()).toBe(1);
    expectGridRequestFor(1);

    viewer.zoomIn();
    viewer.onTouchStart({ touches: [touch(300, 200)] } as unknown as TouchEvent);
    viewer.onTouchEnd({ changedTouches: [touch(150, 205)] } as unknown as TouchEvent);
    expect(viewer.currentPage()).toBe(1);
    http.verify();
  });

  it('notices a replaced document from its tile URLs and reloads it with a notice', () => {
    const viewer = create();
    http.expectOne('/api/documents/doc-1/pages/0/tile-urls')
      .flush({ page: 0, rows: 1, cols: 1, tileSize: 256, tileVersion: 2, tileUrls: [['/api/tiles?token=x']] });
    http.expectOne('/api/documents/doc-1').flush({ ...DOC, pageCount: 3, pages: DOC.pages.slice(0, 3), tileVersion: 2 });
    expect(viewer.updatedNotice()).toBe(true);
    expect(viewer.manifest()?.tileVersion).toBe(2);
    expectGridRequestFor(0);
  });

  it('stops instead of reloading forever when tiles are gone but the document has not changed', () => {
    const viewer = create();
    expectGridRequestFor(0);
    // What the tile loader does after a 410: reload, remembering which render was gone.
    (viewer as unknown as { reloadDocument(page: number, stale: number): void }).reloadDocument(0, 1);
    http.expectOne('/api/documents/doc-1').flush(DOC); // still version 1
    expect(viewer.errorMessage()).toBe('Some parts of this page could not be loaded.');
    expect(viewer.updatedNotice()).toBe(false);
    http.verify(); // no new grid request: no loop
  });

  it('shows the access-lost state when the document stops being available mid-read', () => {
    const viewer = create();
    http.expectOne('/api/documents/doc-1/pages/0/tile-urls').flush({ error: 'Document not found.' }, { status: 404, statusText: 'Not Found' });
    expect(viewer.accessLost()).toBe(true);
  });
});

