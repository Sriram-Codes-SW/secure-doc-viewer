import { LowerCasePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { IdleState, idleState } from './core/idle';
import { SessionService } from './core/session.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, LowerCasePipe],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit, OnDestroy {
  readonly idle = signal<IdleState>({ kind: 'active' });
  private idleTimer: ReturnType<typeof setInterval> | null = null;

  constructor(
    readonly sessionService: SessionService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.idleTimer = setInterval(() => this.checkIdle(), 1000);
  }

  ngOnDestroy(): void {
    if (this.idleTimer !== null) {
      clearInterval(this.idleTimer);
    }
  }

  staySignedIn(): void {
    this.sessionService.staySignedIn().subscribe();
  }

  formatCountdown(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${String(s).padStart(2, '0')}`;
  }

  private checkIdle(): void {
    const user = this.sessionService.user();
    if (!user) {
      this.idle.set({ kind: 'active' });
      return;
    }
    const state = idleState(Date.now(), this.sessionService.lastActivity(), user.sessionTimeoutSeconds);
    this.idle.set(state);
    if (state.kind === 'expired') {
      // The server has already dropped the session; don't wait for the next request to find out.
      this.sessionService.forceLogout();
      const returnUrl = this.router.url.startsWith('/login') ? undefined : this.router.url;
      void this.router.navigate(['/login'], { queryParams: { ...(returnUrl ? { returnUrl } : {}), reason: 'idle' } });
    }
  }

  logout(): void {
    this.sessionService.logout().subscribe({
      complete: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login']),
    });
  }
}
