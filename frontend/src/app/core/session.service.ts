import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, catchError, firstValueFrom, map, of, tap } from 'rxjs';
import { API_BASE_URL } from './config';

export type Role = 'READER' | 'PUBLISHER' | 'ADMIN';

export interface CurrentUser {
  username: string;
  role: Role;
}

/**
 * Who is signed in, as a signal every part of the app reads (nav, guards,
 * interceptor). The credential itself is an httpOnly cookie the browser
 * manages — nothing secret is held here or in web storage — so the session
 * is shared across tabs and survives a reload: on startup we simply ask the
 * server who we are.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly current = signal<CurrentUser | null>(null);

  readonly user = this.current.asReadonly();
  readonly username = computed(() => this.current()?.username ?? null);
  readonly role = computed(() => this.current()?.role ?? null);
  readonly isLoggedIn = computed(() => this.current() !== null);
  readonly isAdmin = computed(() => this.current()?.role === 'ADMIN');
  readonly canUpload = computed(() => this.hasAnyRole('PUBLISHER', 'ADMIN'));

  constructor(private readonly http: HttpClient) {}

  /** Runs once at startup (see app.config.ts). Also primes the CSRF cookie. */
  restore(): Promise<void> {
    return firstValueFrom(
      this.http.get<CurrentUser>(`${API_BASE_URL}/api/auth/me`).pipe(
        tap((user) => this.current.set(user)),
        map(() => undefined),
        catchError(() => {
          this.current.set(null);
          return of(undefined);
        }),
      ),
    );
  }

  login(username: string, password: string): Observable<CurrentUser> {
    return this.http
      .post<CurrentUser>(`${API_BASE_URL}/api/auth/login`, { username, password })
      .pipe(tap((user) => this.current.set(user)));
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/api/auth/logout`, null).pipe(
      tap({ finalize: () => this.current.set(null) }),
    );
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/api/auth/password`, { currentPassword, newPassword });
  }

  hasAnyRole(...roles: Role[]): boolean {
    const role = this.current()?.role;
    return role !== undefined && roles.includes(role);
  }

  /** Called by the HTTP interceptor when the server says the session is gone. */
  forceLogout(): void {
    this.current.set(null);
  }
}
