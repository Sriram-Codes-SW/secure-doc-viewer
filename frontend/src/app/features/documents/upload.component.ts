import { HttpErrorResponse, HttpEventType } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Visibility } from './document.models';
import { DocumentsService } from './documents.service';

/** Mirrors the server limit (spring.servlet.multipart.max-file-size); the server still enforces it. */
export const MAX_UPLOAD_MB = 50;

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
  readonly maxUploadMb = MAX_UPLOAD_MB;
  readonly uploading = signal(false);
  /** 0-100 while the file is being sent; null once the server is rendering pages. */
  readonly uploadPercent = signal<number | null>(null);
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
    this.errorMessage.set(null);
    if (this.file && !/\.pdf$/i.test(this.file.name) && this.file.type !== 'application/pdf') {
      this.errorMessage.set('Please choose a PDF file.');
      this.file = null;
      input.value = '';
      return;
    }
    if (this.file && this.file.size > MAX_UPLOAD_MB * 1024 * 1024) {
      this.errorMessage.set(`That file is ${(this.file.size / 1024 / 1024).toFixed(1)} MB; the limit is ${MAX_UPLOAD_MB} MB.`);
      this.file = null;
      input.value = '';
      return;
    }
    if (this.file && !this.titleEdited) {
      this.title = this.file.name.replace(/\.pdf$/i, '');
    }
  }

  submit(): void {
    if (!this.title.trim() || !this.file) {
      return;
    }
    this.uploading.set(true);
    this.uploadPercent.set(0);
    this.errorMessage.set(null);

    this.documentsService.upload(this.title.trim(), this.file, this.visibility).subscribe({
      next: (event) => {
        if (event.type === HttpEventType.UploadProgress) {
          const percent = event.total ? Math.round((100 * event.loaded) / event.total) : 0;
          // Once every byte is sent, the wait is the server rendering pages.
          this.uploadPercent.set(percent >= 100 ? null : percent);
        } else if (event.type === HttpEventType.Response && event.body) {
          // Straight to the manage page, so a private document can be shared right away.
          this.router.navigate(['/documents', event.body.documentId, 'manage'], { queryParams: { uploaded: 1 } });
        }
      },
      error: (err: HttpErrorResponse) => {
        this.uploading.set(false);
        this.uploadPercent.set(null);
        this.errorMessage.set(err.error?.error ?? 'Upload failed.');
      },
    });
  }
}
