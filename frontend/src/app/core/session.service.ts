import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from './config';

interface StoredSession {
  sessionId: string;
  username: string;
}

interface LoginResponse {
  sessionId: string;
  username: string;
}

const STORAGE_KEY = 'secure-doc-viewer.session';

/**
 * Holds the current login session as a signal so every part of the app
 * (nav bar, route guard, HTTP interceptor) reads the same live value.
 * Persisted to sessionStorage only so a page refresh doesn't force a
 * re-login during a demo — a real app would use an httpOnly cookie instead
 * of anything readable from JS.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly current = signal<StoredSession | null>(this.restore());

  readonly username = computed(() => this.current()?.username ?? null);
  readonly sessionId = computed(() => this.current()?.sessionId ?? null);
  readonly isLoggedIn = computed(() => this.current() !== null);

  constructor(private readonly http: HttpClient) {}

  login(username: string): Observable<LoginResponse> {
    const params = new URLSearchParams({ username });
    return this.http
      .post<LoginResponse>(`${API_BASE_URL}/api/session/login?${params.toString()}`, null)
      .pipe(tap((res) => this.setSession(res.sessionId, res.username)));
  }

  logout(): Observable<void> {
    const sessionId = this.sessionId();
    this.clearSession();
    if (!sessionId) {
      return new Observable((subscriber) => {
        subscriber.next();
        subscriber.complete();
      });
    }
    return this.http.post<void>(`${API_BASE_URL}/api/session/logout`, null, {
      headers: { 'X-Session-Id': sessionId },
    });
  }

  private setSession(sessionId: string, username: string): void {
    this.current.set({ sessionId, username });
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ sessionId, username }));
  }

  private clearSession(): void {
    this.current.set(null);
    sessionStorage.removeItem(STORAGE_KEY);
  }

  private restore(): StoredSession | null {
    try {
      const raw = sessionStorage.getItem(STORAGE_KEY);
      return raw ? (JSON.parse(raw) as StoredSession) : null;
    } catch {
      return null;
    }
  }

  /** Called by the HTTP interceptor when the server says this session is no longer valid. */
  forceLogout(): void {
    this.clearSession();
  }
}
