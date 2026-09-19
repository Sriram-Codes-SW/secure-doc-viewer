import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, OnDestroy, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EMPTY, Subscription, catchError, filter, interval, startWith, switchMap } from 'rxjs';
import { Role } from '../../core/session.service';
import { AdminService } from './admin.service';
import { AUDIT_EVENT_TYPES, AuditEvent, AuditFilter, RateLimitStatus, SessionSummary, UserSummary } from './admin.models';

const REFRESH_MS = 5_000;
/** Background refreshes stop after this long without any input on the page. */
export const POLL_IDLE_MS = 2 * 60_000;

/**
 * Whether to refresh the sessions list in the background. An unattended admin
 * page must not keep polling: every poll would count as activity and keep the
 * most privileged session alive past its idle timeout.
 */
export function shouldPoll(now: number, lastInputAt: number, hidden: boolean): boolean {
  return !hidden && now - lastInputAt < POLL_IDLE_MS;
}
const MIN_PASSWORD_LENGTH = 12;
const AUDIT_PAGE_SIZE = 50;

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css',
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  readonly roles: Role[] = ['READER', 'PUBLISHER', 'ADMIN'];
  readonly minPasswordLength = MIN_PASSWORD_LENGTH;

  readonly sessions = signal<SessionSummary[]>([]);
  readonly users = signal<UserSummary[]>([]);
  readonly auditTypes = AUDIT_EVENT_TYPES;
  readonly auditEvents = signal<AuditEvent[]>([]);
  readonly auditTotal = signal(0);
  readonly auditPage = signal(0);
  readonly auditPageSize = AUDIT_PAGE_SIZE;
  readonly rateLimitStatus = signal<RateLimitStatus | null>(null);
  /** Session handle awaiting a second click to confirm revocation. */
  readonly confirmingRevoke = signal<string | null>(null);
  /** Username whose inline password-reset form is open. */
  readonly resettingUser = signal<string | null>(null);
  /** A role change waiting for confirmation (nothing is sent until confirmed). */
  readonly pendingRole = signal<{ username: string; role: Role } | null>(null);
  /** Username awaiting a second click to confirm disabling. */
  readonly confirmingDisable = signal<string | null>(null);
  readonly userQuery = signal('');
  readonly showDisabled = signal(false);
  readonly filteredUsers = computed(() => {
    const q = this.userQuery().trim().toLowerCase();
    return this.users().filter(
      (u) => (this.showDisabled() || u.enabled) && (!q || u.username.includes(q)),
    );
  });
  readonly hiddenDisabledCount = computed(() =>
    this.showDisabled() ? 0 : this.users().filter((u) => !u.enabled).length,
  );
  readonly usersMessage = signal<{ kind: 'error' | 'success'; text: string } | null>(null);

  selectedUsername = '';
  auditFilter: AuditFilter = { type: '', username: '', documentId: '', trace: '' };
  newUser = { username: '', password: '', role: 'READER' as Role };
  resetPasswordValue = '';

  private sessionsSub: Subscription | null = null;
  private lastInputAt = Date.now();
  private auditSub: Subscription | null = null;

  constructor(private readonly adminService: AdminService) {}

  ngOnInit(): void {
    this.sessionsSub = interval(REFRESH_MS)
      .pipe(
        filter(() => shouldPoll(Date.now(), this.lastInputAt, document.hidden)),
        startWith(0),
        // One failed refresh must not end the polling for good.
        switchMap(() => this.adminService.getSessions().pipe(catchError(() => EMPTY))),
      )
      .subscribe((sessions) => this.sessions.set(sessions));

    this.loadUsers();
    this.refreshAudit();
  }

  @HostListener('document:pointerdown')
  @HostListener('document:keydown')
  @HostListener('document:wheel')
  @HostListener('document:touchstart')
  onUserInput(): void {
    this.lastInputAt = Date.now();
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

  /** Re-runs the audit query; filters changed means back to the first page. */
  refreshAudit(page = 0): void {
    this.auditSub?.unsubscribe();
    this.auditSub = this.adminService.getAudit(this.auditFilter, page, AUDIT_PAGE_SIZE).subscribe((result) => {
      this.auditEvents.set(result.items);
      this.auditTotal.set(result.total);
      this.auditPage.set(result.page);
    });
  }

  clearAuditFilter(): void {
    this.auditFilter = { type: '', username: '', documentId: '', trace: '' };
    this.refreshAudit();
  }

  /** Click a user or document in the log to filter by it. */
  filterAuditBy(change: AuditFilter): void {
    this.auditFilter = { ...this.auditFilter, ...change };
    this.refreshAudit();
  }

  auditExportUrl(): string {
    return this.adminService.auditExportUrl(this.auditFilter);
  }

  auditLastPage(): number {
    return Math.max(0, Math.ceil(this.auditTotal() / AUDIT_PAGE_SIZE) - 1);
  }

  describe(event: AuditEvent): string {
    const doc = event.documentTitle ?? (event.documentId ? event.documentId.slice(0, 8) + '…' : '');
    switch (event.type) {
      case 'PAGE_VIEWED':
        return `${doc} — page ${(event.page ?? 0) + 1}`;
      case 'TILE_VIEWED':
        return `${doc} — page ${(event.page ?? 0) + 1}, tile (${event.tileRow}, ${event.tileCol})`;
      case 'ACCESS_DENIED':
        return `${doc || 'unknown document'} (${event.detail ?? 'view'})`;
      default:
        return [doc, event.detail].filter(Boolean).join(' — ');
    }
  }

  isWarning(event: AuditEvent): boolean {
    return ['SIGN_IN_FAILED', 'SIGN_IN_LOCKED', 'ACCESS_DENIED', 'RATE_LIMITED', 'SESSION_REVOKED'].includes(event.type);
  }

  /** UTC, to match the watermark timestamp and the CSV export. */
  formatMillis(epochMillis: number): string {
    return new Date(epochMillis).toISOString().replace('T', ' ').slice(0, 19) + ' UTC';
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

  /** Selecting a new role only stages it; confirmRoleChange applies it. */
  requestRoleChange(user: UserSummary, role: Role): void {
    this.pendingRole.set(role === user.role ? null : { username: user.username, role });
  }

  cancelRoleChange(): void {
    this.pendingRole.set(null);
    this.loadUsers(); // puts the select back to the saved role
  }

  confirmRoleChange(user: UserSummary): void {
    const pending = this.pendingRole();
    if (!pending || pending.username !== user.username) {
      return;
    }
    this.pendingRole.set(null);
    this.changeRole(user, pending.role);
  }

  readonly confirmingSignOut = signal<string | null>(null);

  signOutEverywhere(user: UserSummary): void {
    if (this.confirmingSignOut() !== user.username) {
      this.confirmingSignOut.set(user.username);
      return;
    }
    this.confirmingSignOut.set(null);
    this.adminService.signOutEverywhere(user.username).subscribe({
      next: () => {
        this.usersMessage.set({ kind: 'success', text: `${user.username} was signed out everywhere.` });
        this.adminService.getSessions().subscribe((sessions) => this.sessions.set(sessions));
      },
      error: (err: HttpErrorResponse) => this.showUsersError(err),
    });
  }

  unlock(user: UserSummary): void {
    this.adminService.unlock(user.username).subscribe({
      next: () => {
        this.usersMessage.set({ kind: 'success', text: `${user.username} can sign in again (lockout cleared).` });
        this.loadUsers();
      },
      error: (err: HttpErrorResponse) => this.showUsersError(err),
    });
  }

  private changeRole(user: UserSummary, role: Role): void {
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
    // Disabling takes two clicks; the first shows what it affects.
    if (user.enabled && this.confirmingDisable() !== user.username) {
      this.confirmingDisable.set(user.username);
      return;
    }
    this.confirmingDisable.set(null);
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

  /** Same format as the audit log and the watermark: UTC, to the minute. */
  formatTime(epochSeconds: number): string {
    return new Date(epochSeconds * 1000).toISOString().slice(0, 16).replace('T', ' ') + ' UTC';
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
