import { Component, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DocumentsService } from './documents.service';
import { DocumentManifest } from './document.models';

@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './document-list.component.html',
  styleUrl: './document-list.component.css',
})
export class DocumentListComponent implements OnInit {
  readonly documents = signal<DocumentManifest[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  constructor(private readonly documentsService: DocumentsService) {}

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
}
