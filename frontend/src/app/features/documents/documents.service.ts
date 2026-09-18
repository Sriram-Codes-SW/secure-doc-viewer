import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/config';
import { DocumentManifest, TileUrlGrid } from './document.models';

/**
 * Everything about a document's identity and structure (what pages exist,
 * how big each one is) — never the pixels themselves. Fetching actual tile
 * pixels is the viewer module's job, deliberately kept separate: this
 * service can be safely cached/reused, tile URLs cannot (they expire).
 */
@Injectable({ providedIn: 'root' })
export class DocumentsService {
  constructor(private readonly http: HttpClient) {}

  list(): Observable<DocumentManifest[]> {
    return this.http.get<DocumentManifest[]>(`${API_BASE_URL}/api/documents`);
  }

  getManifest(documentId: string): Observable<DocumentManifest> {
    return this.http.get<DocumentManifest>(`${API_BASE_URL}/api/documents/${documentId}`);
  }

  upload(title: string, file: File): Observable<DocumentManifest> {
    const formData = new FormData();
    formData.append('title', title);
    formData.append('file', file);
    return this.http.post<DocumentManifest>(`${API_BASE_URL}/api/documents`, formData);
  }

  getTileUrls(documentId: string, page: number): Observable<TileUrlGrid> {
    return this.http.get<TileUrlGrid>(
      `${API_BASE_URL}/api/documents/${documentId}/pages/${page}/tile-urls`,
    );
  }
}
