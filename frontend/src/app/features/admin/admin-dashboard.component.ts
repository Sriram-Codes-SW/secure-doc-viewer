import { SlicePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription, interval, startWith, switchMap } from 'rxjs';
import { AdminService } from './admin.service';
import { AuditEntry, RateLimitStatus, SessionSummary } from './admin.models';

const REFRESH_MS = 5_000;

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [FormsModule, SlicePipe],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css',
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  readonly sessions = signal<SessionSummary[]>([]);
  readonly auditEntries = signal<AuditEntry[]>([]);
  readonly rateLimitStatus = signal<RateLimitStatus | null>(null);
  selectedSessionId = '';
  auditLimit = 50;

  private sessionsSub: Subscription | null = null;
  private auditSub: Subscription | null = null;

  constructor(private readonly adminService: AdminService) {}

  ngOnInit(): void {
    this.sessionsSub = interval(REFRESH_MS)
      .pipe(
        startWith(0),
        switchMap(() => this.adminService.getSessions()),
      )
      .subscribe((sessions) => {
        this.sessions.set(sessions);
        if (!this.selectedSessionId && sessions.length > 0) {
          this.selectedSessionId = sessions[0].sessionId;
          this.checkRateLimit();
        }
      });

    this.refreshAudit();
  }

  ngOnDestroy(): void {
    this.sessionsSub?.unsubscribe();
    this.auditSub?.unsubscribe();
  }

  checkRateLimit(): void {
    if (!this.selectedSessionId) {
      return;
    }
    this.adminService.getRateLimit(this.selectedSessionId).subscribe((status) => {
      this.rateLimitStatus.set(status);
    });
  }

  refreshAudit(): void {
    this.auditSub?.unsubscribe();
    this.auditSub = this.adminService.getAudit(this.auditLimit).subscribe((entries) => {
      this.auditEntries.set(entries);
    });
  }

  formatTime(epochSeconds: number): string {
    return new Date(epochSeconds * 1000).toLocaleString();
  }

  usagePercent(status: RateLimitStatus): number {
    return status.limit === 0 ? 0 : Math.min(100, Math.round((status.used / status.limit) * 100));
  }
}
