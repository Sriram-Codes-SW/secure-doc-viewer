import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_BASE_URL } from './config';
import { SessionService } from './session.service';

/**
 * Attaches X-Session-Id to every request aimed at our own API (never to
 * third-party requests), and reacts to a 401 by clearing the local session
 * and bouncing to /login — the same way any interceptor-driven session
 * expiry redirect works. Tile image requests never go through HttpClient —
 * the viewer fetches them with plain fetch() and no custom headers, since the
 * signed URL alone is the credential — so this interceptor never sees them.
 */
export const sessionInterceptor: HttpInterceptorFn = (req, next) => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  const isOwnApi = req.url.startsWith(API_BASE_URL);
  const sessionId = sessionService.sessionId();

  const authorizedReq =
    isOwnApi && sessionId ? req.clone({ setHeaders: { 'X-Session-Id': sessionId } }) : req;

  return next(authorizedReq).pipe(
    catchError((error: unknown) => {
      if (isOwnApi && error instanceof HttpErrorResponse && error.status === 401) {
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
