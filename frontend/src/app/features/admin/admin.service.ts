import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../core/config';
import { AuditEntry, RateLimitStatus, SessionSummary } from './admin.models';

/**
 * Read-only visibility into state the security modules already maintain
 * (sessions, rate-limit windows, the tile audit trail). This module adds no
 * new access-control decisions of its own — it's purely a window onto what
 * SessionService/TileRateLimiter/AuditLogService enforce server-side.
 */
@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private readonly http: HttpClient) {}

  getSessions(): Observable<SessionSummary[]> {
    return this.http.get<SessionSummary[]>(`${API_BASE_URL}/api/admin/sessions`);
  }

  getRateLimit(targetSessionId: string): Observable<RateLimitStatus> {
    return this.http.get<RateLimitStatus>(`${API_BASE_URL}/api/admin/rate-limit/${targetSessionId}`);
  }

  getAudit(limit: number): Observable<AuditEntry[]> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<AuditEntry[]>(`${API_BASE_URL}/api/admin/audit`, { params });
  }
}
