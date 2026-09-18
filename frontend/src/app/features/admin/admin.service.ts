import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/config';
import { Role } from '../../core/session.service';
import { AuditEntry, RateLimitStatus, SessionSummary, UserSummary } from './admin.models';

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

  getAudit(limit: number): Observable<AuditEntry[]> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<AuditEntry[]>(`${API_BASE_URL}/api/admin/audit`, { params });
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
