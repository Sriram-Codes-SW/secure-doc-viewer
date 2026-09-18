import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { sessionInterceptor } from './core/session.interceptor';
import { SessionService } from './core/session.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    // HttpClient's built-in XSRF support is on by default: it copies the
    // XSRF-TOKEN cookie into the X-XSRF-TOKEN header on same-origin writes,
    // which is exactly what the backend's CSRF protection expects.
    provideHttpClient(withInterceptors([sessionInterceptor])),
    // Resolve "who am I" before the first route guard runs.
    provideAppInitializer(() => inject(SessionService).restore()),
  ],
};
