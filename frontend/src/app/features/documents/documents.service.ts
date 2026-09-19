import { HttpClient, HttpEvent, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/config';
import { DocumentDetail, DocumentSummary, TileUrlGrid, Visibility } from './document.models';

/**
 * Everything about a document's identity, structure and access — never the
 * pixels themselves. Fetching tile pixels is the viewer's job, deliberately
 * kept separate: this data can be cached and reused, tile URLs cannot (they
 * expire and are bound to the session).
 */
@Injectable({ providedIn: 'root' })
export class DocumentsService {
  private readonly base = `${API_BASE_URL}/api/documents`;

  constructor(private readonly http: HttpClient) {}

  list(): Observable<DocumentSummary[]> {
    return this.http.get<DocumentSummary[]>(this.base);
  }

  get(documentId: string): Observable<DocumentDetail> {
    return this.http.get<DocumentDetail>(`${this.base}/${encodeURIComponent(documentId)}`);
  }

  /** Emits upload-progress events, then the created document as the final response. */
  upload(title: string, file: File, visibility: Visibility): Observable<HttpEvent<DocumentDetail>> {
    const formData = new FormData();
    formData.append('title', title);
    formData.append('visibility', visibility);
    formData.append('file', file);
    return this.http.post<DocumentDetail>(this.base, formData, { reportProgress: true, observe: 'events' });
  }

  update(documentId: string, change: { title?: string; visibility?: Visibility }): Observable<DocumentDetail> {
    return this.http.patch<DocumentDetail>(`${this.base}/${encodeURIComponent(documentId)}`, change);
  }

  replaceFile(documentId: string, file: File): Observable<DocumentDetail> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.put<DocumentDetail>(`${this.base}/${encodeURIComponent(documentId)}/file`, formData);
  }

  /** Admin only. */
  transferOwnership(documentId: string, username: string): Observable<DocumentDetail> {
    return this.http.put<DocumentDetail>(`${this.base}/${encodeURIComponent(documentId)}/owner`, { username });
  }

  delete(documentId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${encodeURIComponent(documentId)}`);
  }

  share(documentId: string, username: string): Observable<string[]> {
    return this.http.put<string[]>(
      `${this.base}/${encodeURIComponent(documentId)}/shares/${encodeURIComponent(username)}`,
      null,
    );
  }

  unshare(documentId: string, username: string): Observable<string[]> {
    return this.http.delete<string[]>(
      `${this.base}/${encodeURIComponent(documentId)}/shares/${encodeURIComponent(username)}`,
    );
  }

  /** Username suggestions for the share picker (publishers and admins only). */
  findUsers(prefix: string): Observable<string[]> {
    return this.http.get<string[]>(`${API_BASE_URL}/api/users`, { params: new HttpParams().set('q', prefix) });
  }

  getTileUrls(documentId: string, page: number): Observable<TileUrlGrid> {
    return this.http.get<TileUrlGrid>(`${this.base}/${encodeURIComponent(documentId)}/pages/${page}/tile-urls`);
  }
}
