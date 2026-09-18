import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role, SessionService } from './session.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const sessionService = inject(SessionService);
  const router = inject(Router);

  if (sessionService.isLoggedIn()) {
    return true;
  }
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/**
 * UX only: keeps users off screens their role can't use. The server
 * enforces the same rules on every API call regardless of this guard.
 */
export const roleGuard =
  (...roles: Role[]): CanActivateFn =>
  (route, state) => {
    const signedIn = authGuard(route, state);
    if (signedIn !== true) {
      return signedIn;
    }
    return inject(SessionService).hasAnyRole(...roles) ? true : inject(Router).createUrlTree(['/documents']);
  };
