import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SessionService } from '../../core/session.service';

const MIN_PASSWORD_LENGTH = 12;

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="auth-card">
      <h1>Your account</h1>
      <p class="subtitle">
        Signed in as <strong>{{ sessionService.username() }}</strong> ({{ sessionService.role() }}).
      </p>

      <form (ngSubmit)="submit()">
        <h2>Change password</h2>
        <label for="current">Current password</label>
        <input id="current" name="current" type="password" [(ngModel)]="currentPassword"
               autocomplete="current-password" required />

        <label for="new">New password</label>
        <input id="new" name="new" type="password" [(ngModel)]="newPassword"
               autocomplete="new-password" required [attr.minlength]="minLength" />
        <p class="hint">At least {{ minLength }} characters. Your other signed-in sessions will be signed out.</p>

        <label for="confirm">Confirm new password</label>
        <input id="confirm" name="confirm" type="password" [(ngModel)]="confirmPassword"
               autocomplete="new-password" required />

        @if (errorMessage()) {
          <p class="error" role="alert">{{ errorMessage() }}</p>
        }
        @if (saved()) {
          <p class="success" role="status">Password changed.</p>
        }

        <button type="submit" [disabled]="saving()">{{ saving() ? 'Saving…' : 'Change password' }}</button>
      </form>
    </div>
  `,
  styleUrl: './login.component.css',
})
export class AccountComponent {
  readonly minLength = MIN_PASSWORD_LENGTH;
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  readonly saving = signal(false);
  readonly saved = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor(readonly sessionService: SessionService) {}

  submit(): void {
    this.saved.set(false);
    if (this.newPassword.length < MIN_PASSWORD_LENGTH) {
      this.errorMessage.set(`New password must be at least ${MIN_PASSWORD_LENGTH} characters.`);
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage.set('The new passwords don’t match.');
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    this.sessionService.changePassword(this.currentPassword, this.newPassword).subscribe({
      next: () => {
        this.saving.set(false);
        this.saved.set(true);
        this.currentPassword = this.newPassword = this.confirmPassword = '';
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessage.set(err.error?.error ?? 'Could not change the password.');
      },
    });
  }
}
