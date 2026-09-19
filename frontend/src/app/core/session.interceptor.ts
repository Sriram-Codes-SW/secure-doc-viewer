import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, tap, throwError } from 'rxjs';
import { HttpResponse } from '@angular/common/http';
import { SessionService } from './session.service';

/** Endpoints whose 401 means "not signed in / wrong password", not "session ended". */
const AUTH_PROBES = ['/api/auth/me', '/api/auth/login'];

/**
 * Reacts to a 401 from our API — the session timed out, was signed out
 * elsewhere, or was revoked by an admin — by clearing local state and
 * bouncing to /login with a returnUrl. No credentials are attached here:
 * the session cookie is sent by the browser. Tile images are fetched with
 * plain fetch() by the viewer, which handles its own 401s.
 */
export const sessionInterceptor: HttpInterceptorFn = (req, next) => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  return next(req).pipe(
    tap((event) => {
      if (event instanceof HttpResponse && sessionService.isLoggedIn()) {
        sessionService.touch();
      }
    }),
    catchError((error: unknown) => {
      const isAuthProbe = AUTH_PROBES.some((path) => req.url.endsWith(path));
      if (error instanceof HttpErrorResponse && error.status === 403 && error.error?.passwordChangeRequired) {
        router.navigate(['/account'], { queryParams: { required: 1 } });
      }
      if (!isAuthProbe && error instanceof HttpErrorResponse && error.status === 401) {
        sessionService.forceLogout();
        // Same returnUrl contract as authGuard, so signing back in lands
        // where the user was rather than on the documents list.
        const returnUrl = router.url.startsWith('/login') ? undefined : router.url;
        router.navigate(['/login'], { queryParams: returnUrl ? { returnUrl } : {} });
      }
      return throwError(() => error);
    }),
  );
};
