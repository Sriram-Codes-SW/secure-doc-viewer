import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/config';
import { Role } from '../../core/session.service';
import { AuditFilter, AuditPage, RateLimitStatus, SessionSummary, UserSummary } from './admin.models';

/** ADMIN-only endpoints; the server rejects every call here from any other role. */
@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private readonly http: HttpClient) {}

  getSessions(): Observable<SessionSummary[]> {
    return this.http.get<SessionSummary[]>(`${API_BASE_URL}/api/admin/sessions`);
  }

  revokeSession(handle: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/api/admin/sessions/${encodeURIComponent(handle)}`);
  }

  getRateLimit(username: string): Observable<RateLimitStatus> {
    return this.http.get<RateLimitStatus>(`${API_BASE_URL}/api/admin/rate-limit/${encodeURIComponent(username)}`);
  }

  getAudit(filter: AuditFilter, page: number, size: number): Observable<AuditPage> {
    const params = AdminService.auditParams(filter).set('page', page).set('size', size);
    return this.http.get<AuditPage>(`${API_BASE_URL}/api/admin/audit`, { params });
  }

  /** Same-origin GET, so the session cookie authorises the download; opened as a plain link. */
  auditExportUrl(filter: AuditFilter): string {
    const query = AdminService.auditParams(filter).toString();
    return `${API_BASE_URL}/api/admin/audit/export${query ? '?' + query : ''}`;
  }

  private static auditParams(filter: AuditFilter): HttpParams {
    let params = new HttpParams();
    if (filter.type) {
      params = params.set('type', filter.type);
    }
    if (filter.username?.trim()) {
      params = params.set('username', filter.username.trim());
    }
    if (filter.documentId?.trim()) {
      params = params.set('documentId', filter.documentId.trim());
    }
    return params;
  }

  getUsers(): Observable<UserSummary[]> {
    return this.http.get<UserSummary[]>(`${API_BASE_URL}/api/admin/users`);
  }

  createUser(username: string, password: string, role: Role): Observable<UserSummary> {
    return this.http.post<UserSummary>(`${API_BASE_URL}/api/admin/users`, { username, password, role });
  }

  updateUser(username: string, change: { role?: Role; enabled?: boolean }): Observable<UserSummary> {
    return this.http.patch<UserSummary>(`${API_BASE_URL}/api/admin/users/${encodeURIComponent(username)}`, change);
  }

  resetPassword(username: string, password: string): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/api/admin/users/${encodeURIComponent(username)}/password`, {
      password,
    });
  }
}
