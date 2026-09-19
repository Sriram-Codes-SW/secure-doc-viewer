import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, catchError, firstValueFrom, map, of, tap } from 'rxjs';
import { API_BASE_URL } from './config';

export type Role = 'READER' | 'PUBLISHER' | 'ADMIN';

export interface CurrentUser {
  username: string;
  role: Role;
  /** Server-side idle timeout; every API request (including tile fetches) resets it. */
  sessionTimeoutSeconds: number;
  /** Password was set by an admin: the server refuses everything else until it is changed. */
  mustChangePassword: boolean;
}

/** Shared by every tab, so activity in one tab keeps the others from warning. Not sensitive. */
const LAST_ACTIVITY_KEY = 'sdv.lastActivity';

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
  private readonly activity = signal(Date.now());
  /** Epoch ms of the latest request any tab made to the API. */
  readonly lastActivity = this.activity.asReadonly();

  readonly user = this.current.asReadonly();
  readonly username = computed(() => this.current()?.username ?? null);
  readonly role = computed(() => this.current()?.role ?? null);
  readonly isLoggedIn = computed(() => this.current() !== null);
  readonly isAdmin = computed(() => this.current()?.role === 'ADMIN');
  readonly canUpload = computed(() => this.hasAnyRole('PUBLISHER', 'ADMIN'));
  readonly mustChangePassword = computed(() => this.current()?.mustChangePassword ?? false);

  constructor(private readonly http: HttpClient) {
    window.addEventListener('storage', (event) => {
      if (event.key === LAST_ACTIVITY_KEY && event.newValue) {
        this.activity.set(Math.max(this.activity(), Number(event.newValue)));
      }
    });
  }

  /** Called after every successful API request; the server has just extended the session. */
  touch(): void {
    const now = Date.now();
    this.activity.set(now);
    try {
      localStorage.setItem(LAST_ACTIVITY_KEY, String(now));
    } catch {
      // Other tabs just won't hear about it; this tab still tracks its own activity.
    }
  }

  /** Any authenticated request extends the session; /me is the cheapest. */
  staySignedIn(): Observable<void> {
    return this.http.get<CurrentUser>(`${API_BASE_URL}/api/auth/me`).pipe(map(() => undefined));
  }

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
    return this.http.post<void>(`${API_BASE_URL}/api/auth/password`, { currentPassword, newPassword }).pipe(
      tap(() => {
        const user = this.current();
        if (user) {
          this.current.set({ ...user, mustChangePassword: false });
        }
      }),
    );
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
