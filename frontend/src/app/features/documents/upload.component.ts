import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Visibility } from './document.models';
import { DocumentsService } from './documents.service';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './upload.component.html',
  styleUrl: './upload.component.css',
})
export class UploadComponent {
  title = '';
  visibility: Visibility = 'PRIVATE';
  file: File | null = null;
  readonly uploading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  /** True once the user has typed their own title, so picking a new file doesn't overwrite it. */
  private titleEdited = false;

  constructor(
    private readonly documentsService: DocumentsService,
    private readonly router: Router,
  ) {}

  onTitleInput(): void {
    this.titleEdited = this.title.trim().length > 0;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.file = input.files?.[0] ?? null;
    if (this.file && !this.titleEdited) {
      this.title = this.file.name.replace(/\.pdf$/i, '');
    }
  }

  submit(): void {
    if (!this.title.trim() || !this.file) {
      return;
    }
    this.uploading.set(true);
    this.errorMessage.set(null);

    this.documentsService.upload(this.title.trim(), this.file, this.visibility).subscribe({
      // Straight to the manage page, so a private document can be shared right away.
      next: (created) => this.router.navigate(['/documents', created.documentId, 'manage'], {
        queryParams: { uploaded: 1 },
      }),
      error: (err: HttpErrorResponse) => {
        this.uploading.set(false);
        this.errorMessage.set(err.error?.error ?? 'Upload failed.');
      },
    });
  }
}
