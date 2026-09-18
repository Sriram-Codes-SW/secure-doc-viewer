import { HttpErrorResponse } from '@angular/common/http';
import { SlicePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription, interval, startWith, switchMap } from 'rxjs';
import { Role } from '../../core/session.service';
import { AdminService } from './admin.service';
import { AuditEntry, RateLimitStatus, SessionSummary, UserSummary } from './admin.models';

const REFRESH_MS = 5_000;
const MIN_PASSWORD_LENGTH = 12;

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [FormsModule, SlicePipe],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css',
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  readonly roles: Role[] = ['READER', 'PUBLISHER', 'ADMIN'];
  readonly minPasswordLength = MIN_PASSWORD_LENGTH;

  readonly sessions = signal<SessionSummary[]>([]);
  readonly users = signal<UserSummary[]>([]);
  readonly auditEntries = signal<AuditEntry[]>([]);
  readonly rateLimitStatus = signal<RateLimitStatus | null>(null);
  /** Session handle awaiting a second click to confirm revocation. */
  readonly confirmingRevoke = signal<string | null>(null);
  /** Username whose inline password-reset form is open. */
  readonly resettingUser = signal<string | null>(null);
  readonly usersMessage = signal<{ kind: 'error' | 'success'; text: string } | null>(null);

  selectedUsername = '';
  auditLimit = 50;
  newUser = { username: '', password: '', role: 'READER' as Role };
  resetPasswordValue = '';

  private sessionsSub: Subscription | null = null;
  private auditSub: Subscription | null = null;

  constructor(private readonly adminService: AdminService) {}

  ngOnInit(): void {
    this.sessionsSub = interval(REFRESH_MS)
      .pipe(
        startWith(0),
        switchMap(() => this.adminService.getSessions()),
      )
      .subscribe((sessions) => this.sessions.set(sessions));

    this.loadUsers();
    this.refreshAudit();
  }

  ngOnDestroy(): void {
    this.sessionsSub?.unsubscribe();
    this.auditSub?.unsubscribe();
  }

  revoke(session: SessionSummary): void {
    if (this.confirmingRevoke() !== session.handle) {
      this.confirmingRevoke.set(session.handle);
      return;
    }
    this.confirmingRevoke.set(null);
    this.adminService.revokeSession(session.handle).subscribe(() =>
      this.sessions.update((all) => all.filter((s) => s.handle !== session.handle)),
    );
  }

  checkRateLimit(): void {
    if (!this.selectedUsername) {
      return;
    }
    this.adminService.getRateLimit(this.selectedUsername).subscribe((status) => this.rateLimitStatus.set(status));
  }

  refreshAudit(): void {
    this.auditSub?.unsubscribe();
    this.auditSub = this.adminService.getAudit(this.auditLimit).subscribe((entries) => this.auditEntries.set(entries));
  }

  createUser(): void {
    const { username, password, role } = this.newUser;
    if (password.length < MIN_PASSWORD_LENGTH) {
      this.usersMessage.set({ kind: 'error', text: `Password must be at least ${MIN_PASSWORD_LENGTH} characters.` });
      return;
    }
    this.adminService.createUser(username, password, role).subscribe({
      next: (created) => {
        this.usersMessage.set({ kind: 'success', text: `Created ${created.username} (${created.role}).` });
        this.newUser = { username: '', password: '', role: 'READER' };
        this.loadUsers();
      },
      error: (err: HttpErrorResponse) => this.showUsersError(err),
    });
  }

  changeRole(user: UserSummary, role: Role): void {
    this.adminService.updateUser(user.username, { role }).subscribe({
      next: () => {
        this.usersMessage.set({ kind: 'success', text: `${user.username} is now ${role}; their sessions were ended.` });
        this.loadUsers();
      },
      error: (err: HttpErrorResponse) => {
        this.showUsersError(err);
        this.loadUsers();
      },
    });
  }

  toggleEnabled(user: UserSummary): void {
    this.adminService.updateUser(user.username, { enabled: !user.enabled }).subscribe({
      next: () => {
        this.usersMessage.set({
          kind: 'success',
          text: `${user.username} ${user.enabled ? 'disabled and signed out' : 'enabled'}.`,
        });
        this.loadUsers();
      },
      error: (err: HttpErrorResponse) => this.showUsersError(err),
    });
  }

  startReset(user: UserSummary): void {
    this.resettingUser.set(user.username);
    this.resetPasswordValue = '';
  }

  submitReset(user: UserSummary): void {
    if (this.resetPasswordValue.length < MIN_PASSWORD_LENGTH) {
      this.usersMessage.set({ kind: 'error', text: `Password must be at least ${MIN_PASSWORD_LENGTH} characters.` });
      return;
    }
    this.adminService.resetPassword(user.username, this.resetPasswordValue).subscribe({
      next: () => {
        this.resettingUser.set(null);
        this.resetPasswordValue = '';
        this.usersMessage.set({ kind: 'success', text: `Password reset for ${user.username}; they were signed out.` });
      },
      error: (err: HttpErrorResponse) => this.showUsersError(err),
    });
  }

  formatTime(epochSeconds: number): string {
    return new Date(epochSeconds * 1000).toLocaleString();
  }

  usagePercent(status: RateLimitStatus): number {
    return status.limit === 0 ? 0 : Math.min(100, Math.round((status.used / status.limit) * 100));
  }

  private loadUsers(): void {
    this.adminService.getUsers().subscribe((users) => {
      this.users.set(users);
      if (!this.selectedUsername && users.length > 0) {
        this.selectedUsername = users[0].username;
        this.checkRateLimit();
      }
    });
  }

  private showUsersError(err: HttpErrorResponse): void {
    this.usersMessage.set({ kind: 'error', text: err.error?.error ?? 'Something went wrong.' });
  }
}
