import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, Subject, Subscription, catchError, debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { SessionService } from '../../core/session.service';
import { DocumentDetail, Visibility } from './document.models';
import { DocumentsService } from './documents.service';

type Notice = { kind: 'error' | 'success'; text: string } | null;

/** Owner/admin screen for one document. The server re-checks every action. */
@Component({
  selector: 'app-manage-document',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './manage.component.html',
  styleUrl: './manage.component.css',
})
export class ManageDocumentComponent implements OnInit, OnDestroy {
  readonly document = signal<DocumentDetail | null>(null);
  readonly loadError = signal<string | null>(null);
  readonly justUploaded = signal(false);
  readonly notice = signal<Notice>(null);
  readonly suggestions = signal<string[]>([]);
  readonly busy = signal(false);
  readonly confirmingDelete = signal(false);
  /** Errors from sharing are shown next to the share box, not at the top of the page. */
  readonly shareError = signal<string | null>(null);

  title = '';
  visibility: Visibility = 'PRIVATE';
  shareWith = '';
  replacement: File | null = null;
  newOwner = '';

  private documentId = '';
  private readonly userQuery = new Subject<string>();
  private userQuerySub: Subscription | null = null;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly documentsService: DocumentsService,
    readonly sessionService: SessionService,
  ) {}

  ngOnInit(): void {
    this.documentId = this.route.snapshot.paramMap.get('documentId') ?? '';
    this.justUploaded.set(this.route.snapshot.queryParamMap.has('uploaded'));
    this.documentsService.get(this.documentId).subscribe({
      next: (doc) => {
        if (!doc.canManage) {
          this.loadError.set('Only the owner or an admin can manage this document.');
          return;
        }
        this.apply(doc);
      },
      error: (err: HttpErrorResponse) =>
        this.loadError.set(
          err.status === 404 ? 'This document doesn’t exist, or it hasn’t been shared with you.' : 'Could not load this document.',
        ),
    });

    this.userQuerySub = this.userQuery
      .pipe(
        debounceTime(200),
        distinctUntilChanged(),
        // The server returns nothing under two characters; don't even ask.
        switchMap((q) => (q.trim().length >= 2 ? this.documentsService.findUsers(q).pipe(catchError(() => of([]))) : of([]))),
      )
      .subscribe((names) => {
        const shared = new Set(this.document()?.sharedWith ?? []);
        this.suggestions.set(names.filter((n) => n !== this.document()?.owner && !shared.has(n)));
      });
  }

  ngOnDestroy(): void {
    this.userQuerySub?.unsubscribe();
  }

  onShareInput(value: string): void {
    this.userQuery.next(value);
  }

  saveDetails(): void {
    const doc = this.document();
    if (!doc) {
      return;
    }
    const change: { title?: string; visibility?: Visibility } = {};
    if (this.title.trim() !== doc.title) {
      change.title = this.title.trim();
    }
    if (this.visibility !== doc.visibility) {
      change.visibility = this.visibility;
    }
    if (Object.keys(change).length === 0) {
      this.notice.set({ kind: 'success', text: 'Nothing to save.' });
      return;
    }
    this.run(this.documentsService.update(this.documentId, change), (updated) => {
      this.apply(updated);
      this.notice.set({ kind: 'success', text: 'Saved.' });
    });
  }

  share(): void {
    const username = this.shareWith.trim();
    if (!username) {
      return;
    }
    this.shareError.set(null);
    this.busy.set(true);
    this.documentsService.share(this.documentId, username).subscribe({
      next: (sharedWith) => {
        this.busy.set(false);
        this.patchShares(sharedWith);
        this.shareWith = '';
        this.suggestions.set([]);
        this.notice.set({ kind: 'success', text: `Shared with ${username.toLowerCase()}.` });
      },
      error: (err: HttpErrorResponse) => {
        this.busy.set(false);
        this.shareError.set(err.error?.error ?? 'Could not share.');
      },
    });
  }

  unshare(username: string): void {
    this.run(this.documentsService.unshare(this.documentId, username), (sharedWith) => {
      this.patchShares(sharedWith);
      this.notice.set({ kind: 'success', text: `${username} can no longer open it — including pages already open.` });
    });
  }

  transferOwnership(): void {
    const username = this.newOwner.trim();
    if (!username) {
      return;
    }
    this.run(this.documentsService.transferOwnership(this.documentId, username), (updated) => {
      this.apply(updated);
      this.newOwner = '';
      this.notice.set({ kind: 'success', text: `${updated.owner} now owns this document.` });
    });
  }

  onReplacementSelected(event: Event): void {
    this.replacement = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  replaceFile(): void {
    if (!this.replacement) {
      return;
    }
    this.run(this.documentsService.replaceFile(this.documentId, this.replacement), (updated) => {
      this.apply(updated);
      this.replacement = null;
      this.notice.set({ kind: 'success', text: `PDF replaced — now ${updated.pageCount} pages. Sharing is unchanged.` });
    });
  }

  deleteDocument(): void {
    if (!this.confirmingDelete()) {
      this.confirmingDelete.set(true);
      return;
    }
    this.run(this.documentsService.delete(this.documentId), () => this.router.navigate(['/documents']));
  }

  private apply(doc: DocumentDetail): void {
    this.document.set(doc);
    this.title = doc.title;
    this.visibility = doc.visibility;
  }

  private patchShares(sharedWith: string[]): void {
    const doc = this.document();
    if (doc) {
      this.document.set({ ...doc, sharedWith });
    }
  }

  private run<T>(request: Observable<T>, onSuccess: (value: T) => void): void {
    this.busy.set(true);
    this.notice.set(null);
    request.subscribe({
      next: (value) => {
        this.busy.set(false);
        onSuccess(value);
      },
      error: (err: HttpErrorResponse) => {
        this.busy.set(false);
        this.confirmingDelete.set(false);
        this.notice.set({ kind: 'error', text: err.error?.error ?? 'Something went wrong.' });
      },
    });
  }
}
