import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { authGuard, roleGuard } from './auth.guard';
import { SessionService } from './session.service';

describe('route guards', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] });
  });

  function signIn(role: 'READER' | 'ADMIN', mustChangePassword: boolean): void {
    TestBed.inject(SessionService).login('someone', 'pw').subscribe();
    TestBed.inject(HttpTestingController).expectOne('/api/auth/login')
      .flush({ username: 'someone', role, sessionTimeoutSeconds: 1800, mustChangePassword });
  }

  function run(guard: typeof authGuard, url: string): boolean | UrlTree {
    return TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, { url } as RouterStateSnapshot),
    ) as boolean | UrlTree;
  }

  function path(result: boolean | UrlTree): string {
    return result instanceof UrlTree ? TestBed.inject(Router).serializeUrl(result) : String(result);
  }

  it('sends signed-out users to login with a return URL', () => {
    expect(path(run(authGuard, '/viewer/abc?page=3'))).toBe('/login?returnUrl=%2Fviewer%2Fabc%3Fpage%3D3');
  });

  it('sends a user with an admin-set password to the account page first', () => {
    signIn('READER', true);
    expect(path(run(authGuard, '/documents'))).toBe('/account?required=1&returnUrl=%2Fdocuments');
    expect(run(authGuard, '/account')).toBe(true);
  });

  it('keeps readers out of admin screens', () => {
    signIn('READER', false);
    expect(path(run(roleGuard('ADMIN'), '/admin'))).toBe('/documents');
    expect(run(authGuard, '/documents')).toBe(true);
  });
});
