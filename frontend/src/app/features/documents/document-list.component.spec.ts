import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { DocumentListComponent } from './document-list.component';
import { DocumentSummary } from './document.models';

function doc(overrides: Partial<DocumentSummary>): DocumentSummary {
  return {
    documentId: 'id-' + Math.random(),
    title: 'Untitled',
    pageCount: 1,
    owner: 'someone',
    visibility: 'PRIVATE',
    createdAtEpochSeconds: 0,
    updatedAtEpochSeconds: 0,
    canManage: false,
    sharedWithCount: null,
    ...overrides,
  };
}

describe('DocumentListComponent', () => {
  let component: DocumentListComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DocumentListComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    component = TestBed.createComponent(DocumentListComponent).componentInstance;
    component.ngOnInit();
    TestBed.inject(HttpTestingController)
      .expectOne('/api/documents')
      .flush([
        doc({ title: 'Quarterly report', owner: 'alice' }),
        doc({ title: 'Handbook', owner: 'bob', visibility: 'EVERYONE' }),
      ]);
  });

  it('filters by title or owner, case-insensitively', () => {
    component.query.set('REPORT');
    expect(component.filtered().map((d) => d.title)).toEqual(['Quarterly report']);

    component.query.set('bob');
    expect(component.filtered().map((d) => d.title)).toEqual(['Handbook']);

    component.query.set('');
    expect(component.filtered().length).toBe(2);
  });

  it('labels who can see each document', () => {
    expect(component.accessLabel(doc({ visibility: 'EVERYONE' }))).toBe('Everyone');
    expect(component.accessLabel(doc({ sharedWithCount: null }))).toBe('Shared with you');
    expect(component.accessLabel(doc({ canManage: true, sharedWithCount: 0 }))).toBe('Private');
    expect(component.accessLabel(doc({ canManage: true, sharedWithCount: 1 }))).toBe('Shared with 1 person');
    expect(component.accessLabel(doc({ canManage: true, sharedWithCount: 3 }))).toBe('Shared with 3 people');
  });
});
