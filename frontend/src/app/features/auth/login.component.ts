import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SessionService } from '../../core/session.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  username = '';
  password = '';
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor(
    private readonly sessionService: SessionService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
  ) {}

  submit(): void {
    if (!this.username.trim() || !this.password) {
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);

    this.sessionService.login(this.username.trim(), this.password).subscribe({
      next: () => {
        this.password = '';
        this.router.navigateByUrl(safeReturnUrl(this.route.snapshot.queryParamMap.get('returnUrl')));
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.password = '';
        this.errorMessage.set(loginErrorMessage(err));
      },
    });
  }
}

/** Only ever navigate within the app — a crafted ?returnUrl=//evil.example must not redirect off-site. */
function safeReturnUrl(returnUrl: string | null): string {
  return returnUrl && returnUrl.startsWith('/') && !returnUrl.startsWith('//') ? returnUrl : '/documents';
}

function loginErrorMessage(err: HttpErrorResponse): string {
  if (err.status === 429) {
    const minutes = Math.max(1, Math.ceil(Number(err.headers.get('Retry-After') ?? 60) / 60));
    return `Too many failed attempts. Try again in about ${minutes} minute${minutes === 1 ? '' : 's'}.`;
  }
  if (err.status === 401) {
    return 'Incorrect username or password.';
  }
  return 'Sign-in failed. Please try again.';
}
