import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { DocumentDetail } from './document.models';
import { ManageDocumentComponent } from './manage.component';
import { UploadComponent } from './upload.component';

const DOC: DocumentDetail = {
  documentId: 'doc-1', title: 'Doc', pageCount: 1, owner: 'pub', visibility: 'PRIVATE',
  createdAtEpochSeconds: 0, updatedAtEpochSeconds: 0, canManage: true, sharedWith: ['reader'],
  pages: [], tileVersion: 1,
};

describe('ManageDocumentComponent sharing', () => {
  it('asks before removing someone, and only then calls the server', () => {
    TestBed.configureTestingModule({
      imports: [ManageDocumentComponent],
      providers: [
        provideRouter([]), provideHttpClient(), provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ documentId: 'doc-1' }), queryParamMap: convertToParamMap({}) } },
        },
      ],
    });
    const http = TestBed.inject(HttpTestingController);
    const manage = TestBed.createComponent(ManageDocumentComponent).componentInstance;
    manage.ngOnInit();
    http.expectOne('/api/documents/doc-1').flush(DOC);

    manage.unshare('reader');
    expect(manage.confirmingUnshare()).toBe('reader');
    http.expectNone((req) => req.method === 'DELETE');

    manage.unshare('reader');
    http.expectOne((req) => req.method === 'DELETE' && req.url.endsWith('/doc-1/shares/reader')).flush([]);
    expect(manage.document()?.sharedWith).toEqual([]);
  });
});

describe('UploadComponent file checks', () => {
  function pick(upload: UploadComponent, file: File): void {
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [file] });
    upload.onFileSelected({ target: input } as unknown as Event);
  }

  function create(): UploadComponent {
    TestBed.configureTestingModule({
      imports: [UploadComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    return TestBed.createComponent(UploadComponent).componentInstance;
  }

  it('refuses a file that is not a PDF before uploading anything', () => {
    const upload = create();
    pick(upload, new File(['hello'], 'notes.txt', { type: 'text/plain' }));
    expect(upload.errorMessage()).toBe('Please choose a PDF file.');
  });

  it('refuses a PDF over the size limit and says how big it was', () => {
    const upload = create();
    const big = new File(['x'], 'big.pdf', { type: 'application/pdf' });
    Object.defineProperty(big, 'size', { value: 60 * 1024 * 1024 });
    pick(upload, big);
    expect(upload.errorMessage()).toBe('That file is 60.0 MB; the limit is 50 MB.');
  });

  it('suggests a title from the file name', () => {
    const upload = create();
    pick(upload, new File(['%PDF-'], 'Quarterly Report.pdf', { type: 'application/pdf' }));
    expect(upload.errorMessage()).toBeNull();
    expect(upload.title).toBe('Quarterly Report');
  });
});
