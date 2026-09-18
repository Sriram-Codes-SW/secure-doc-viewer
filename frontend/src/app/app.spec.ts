import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { routes } from './app.routes';
import { Role, SessionService } from './core/session.service';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  function signInAs(role: Role): void {
    TestBed.inject(SessionService).login('someone', 'irrelevant-password').subscribe();
    TestBed.inject(HttpTestingController).expectOne('/api/auth/login').flush({ username: 'someone', role, sessionTimeoutSeconds: 1800 });
  }

  async function render(): Promise<HTMLElement> {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('should create the app', () => {
    expect(TestBed.createComponent(App).componentInstance).toBeTruthy();
  });

  it('shows no navigation when signed out', async () => {
    const page = await render();
    expect(page.querySelector('nav')).toBeNull();
  });

  it('hides the Admin link from readers', async () => {
    signInAs('READER');
    const page = await render();
    const links = Array.from(page.querySelectorAll('nav a')).map((a) => a.textContent?.trim());
    expect(links).toEqual(['Documents']);
  });

  it('shows the Admin link to admins', async () => {
    signInAs('ADMIN');
    const page = await render();
    const links = Array.from(page.querySelectorAll('nav a')).map((a) => a.textContent?.trim());
    expect(links).toEqual(['Documents', 'Admin']);
  });
});
