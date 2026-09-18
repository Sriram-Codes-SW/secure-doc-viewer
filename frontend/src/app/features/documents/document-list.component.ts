import { Component, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SessionService } from '../../core/session.service';
import { DocumentsService } from './documents.service';
import { DocumentSummary } from './document.models';

@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './document-list.component.html',
  styleUrl: './document-list.component.css',
})
export class DocumentListComponent implements OnInit {
  readonly documents = signal<DocumentSummary[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly query = signal('');

  /** Client-side filter on title and owner; the server already returned only what this user may see. */
  readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    return q
      ? this.documents().filter((d) => d.title.toLowerCase().includes(q) || d.owner.toLowerCase().includes(q))
      : this.documents();
  });

  constructor(
    private readonly documentsService: DocumentsService,
    readonly sessionService: SessionService,
  ) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    this.documentsService.list().subscribe({
      next: (docs) => {
        this.documents.set(docs);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Could not load documents.');
        this.loading.set(false);
      },
    });
  }

  accessLabel(doc: DocumentSummary): string {
    if (doc.visibility === 'EVERYONE') {
      return 'Everyone';
    }
    if (doc.sharedWithCount === null) {
      return 'Shared with you';
    }
    return doc.sharedWithCount === 0
      ? 'Private'
      : `Shared with ${doc.sharedWithCount} ${doc.sharedWithCount === 1 ? 'person' : 'people'}`;
  }

  formatDate(epochSeconds: number): string {
    return new Date(epochSeconds * 1000).toLocaleDateString();
  }
}
