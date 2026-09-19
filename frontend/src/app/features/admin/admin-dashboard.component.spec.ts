import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { UserSummary } from './admin.models';
import { AdminDashboardComponent, POLL_IDLE_MS, shouldPoll } from './admin-dashboard.component';

function user(username: string, overrides: Partial<UserSummary> = {}): UserSummary {
  return {
    username, role: 'READER', enabled: true, createdAtEpochSeconds: 0, lastSignInEpochSeconds: null,
    mustChangePassword: false, ownedDocuments: 0, locked: false, ...overrides,
  };
}

describe('AdminDashboardComponent users', () => {
  let http: HttpTestingController;

  function create(users: UserSummary[]) {
    TestBed.configureTestingModule({
      imports: [AdminDashboardComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(AdminDashboardComponent);
    fixture.detectChanges();
    http.match((req) => req.url.endsWith('/api/admin/users')).forEach((req) => req.flush(users));
    // Sessions and audit panels load too; they're not under test here.
    http.match(() => true).forEach((req) => req.flush(req.request.url.includes('audit')
      ? { items: [], page: 0, size: 50, total: 0 } : []));
    fixture.detectChanges();
    return fixture;
  }

  it('hides disabled accounts until asked, and says how many are hidden', () => {
    const fixture = create([user('alice'), user('old.tester', { enabled: false })]);
    const admin = fixture.componentInstance;
    expect(admin.filteredUsers().map((u) => u.username)).toEqual(['alice']);
    expect(admin.hiddenDisabledCount()).toBe(1);

    admin.showDisabled.set(true);
    expect(admin.filteredUsers().map((u) => u.username)).toEqual(['alice', 'old.tester']);
  });

  it('offers Unlock only for a locked account', () => {
    const fixture = create([user('alice'), user('bob', { locked: true })]);
    const rows = Array.from(fixture.nativeElement.querySelectorAll('tbody tr')) as HTMLElement[];
    const unlockIn = (name: string) => {
      const row = rows.find((r) => r.textContent?.includes(name))!;
      return Array.from(row.querySelectorAll('button')).some((b) => b.textContent?.trim() === 'Unlock');
    };
    expect(unlockIn('alice')).toBe(false);
    expect(unlockIn('bob')).toBe(true);
  });

  it('shows every time in UTC, like the audit log and the watermark', () => {
    const admin = create([]).componentInstance;
    expect(admin.formatTime(Date.UTC(2026, 8, 18, 23, 22, 45) / 1000)).toBe('2026-09-18 23:22 UTC');
  });
});

describe('admin sessions polling', () => {
  const now = 10_000_000;

  it('refreshes while someone is using the visible page', () => {
    expect(shouldPoll(now, now - 30_000, false)).toBe(true);
  });

  it('stops when the page has been left alone, so polls never keep an idle admin signed in', () => {
    expect(shouldPoll(now, now - POLL_IDLE_MS, false)).toBe(false);
  });

  it('stops while the tab is hidden', () => {
    expect(shouldPoll(now, now, true)).toBe(false);
  });
});
