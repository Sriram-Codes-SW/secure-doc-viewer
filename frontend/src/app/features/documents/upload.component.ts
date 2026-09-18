import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
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
  file: File | null = null;
  readonly uploading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor(
    private readonly documentsService: DocumentsService,
    private readonly router: Router,
  ) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.file = input.files?.[0] ?? null;
  }

  submit(): void {
    if (!this.title.trim() || !this.file) {
      return;
    }
    this.uploading.set(true);
    this.errorMessage.set(null);

    this.documentsService.upload(this.title.trim(), this.file).subscribe({
      next: (manifest) => this.router.navigate(['/viewer', manifest.documentId]),
      error: (err: HttpErrorResponse) => {
        this.uploading.set(false);
        this.errorMessage.set(err.error?.error ?? 'Upload failed.');
      },
    });
  }
}
