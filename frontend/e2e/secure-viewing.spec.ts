import AxeBuilder from '@axe-core/playwright';
import { APIRequestContext, Browser, Page, expect, request, test } from '@playwright/test';

const ADMIN_USER = process.env['E2E_ADMIN_USER'] ?? 'admin';
const ADMIN_PASSWORD = process.env['E2E_ADMIN_PASSWORD'] ?? '';

// Unique per run, so the suite can be re-run against the same database.
const run = Date.now().toString(36);
// Admin-set passwords are temporary: each user must choose their own on first sign-in.
const publisher = { username: `e2e-pub-${run}`, temporary: 'e2e-publisher-temp', password: 'e2e-publisher-password', role: 'PUBLISHER' };
const reader = { username: `e2e-reader-${run}`, temporary: 'e2e-reader-temp', password: 'e2e-reader-password', role: 'READER' };
const outsider = { username: `e2e-outsider-${run}`, temporary: 'e2e-outsider-temp', password: 'e2e-outsider-password', role: 'READER' };
type NewUser = typeof publisher;

/** No serious or critical WCAG 2.1 A/AA violations on the current screen, in light and dark themes. */
async function expectAccessible(page: Page, screen: string): Promise<void> {
  for (const colorScheme of ['light', 'dark'] as const) {
    await page.emulateMedia({ colorScheme });
    const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']).analyze();
    const serious = results.violations
      .filter((v) => v.impact === 'serious' || v.impact === 'critical')
      .map((v) => `${v.id}: ${v.help} (${v.nodes.map((n) => n.target.join(' ')).join(', ')})`);
    expect(serious, `accessibility on ${screen} (${colorScheme})`).toEqual([]);
  }
  await page.emulateMedia({ colorScheme: 'light' });
}

async function submitSignIn(browser: Browser, username: string, password: string): Promise<Page> {
  const page = await (await browser.newContext()).newPage();
  await page.goto('/login');
  await page.fill('#username', username);
  await page.fill('#password', password);
  await page.click('button[type=submit]');
  return page;
}

async function signIn(browser: Browser, username: string, password: string): Promise<Page> {
  const page = await submitSignIn(browser, username, password);
  await expect(page).toHaveURL(/\/documents/);
  return page;
}

/** First sign-in with an admin-set password: nothing else is reachable until it is replaced. */
async function firstSignIn(browser: Browser, user: NewUser): Promise<Page> {
  const page = await submitSignIn(browser, user.username, user.temporary);
  await expect(page).toHaveURL(/\/account\?.*required=1/);
  await page.goto('/documents');
  await expect(page).toHaveURL(/\/account\?.*required=1/);
  await page.fill('#current', user.temporary);
  await page.fill('#new', user.password);
  await page.fill('#confirm', user.password);
  await page.click('button[type=submit]');
  await expect(page).toHaveURL(/\/documents/);
  return page;
}

test('a publisher shares a document with one reader, and nobody else can see it', async ({ browser }) => {
  test.skip(!ADMIN_PASSWORD, 'Set E2E_ADMIN_PASSWORD to run end-to-end tests');

  // Admin creates the accounts (there is no self-signup).
  const loginPage = await (await browser.newContext()).newPage();
  await loginPage.goto('/login');
  await expectAccessible(loginPage, 'sign-in');
  const admin = await signIn(browser, ADMIN_USER, ADMIN_PASSWORD);
  adminPage = admin;
  await admin.goto('/admin');
  await expect(admin.getByRole('heading', { name: /users/i }).first()).toBeVisible();
  await expectAccessible(admin, 'admin');
  for (const user of [publisher, reader, outsider]) {
    await admin.fill('input[name=newUsername]', user.username);
    await admin.fill('input[name=newPassword]', user.temporary);
    await admin.selectOption('select[name=newRole]', user.role);
    await admin.click('button:has-text("Create user")');
    await expect(admin.getByText(`Created ${user.username}`)).toBeVisible();
  }

  // A two-page PDF, generated rather than committed.
  const pdfPage = await admin.context().newPage();
  await pdfPage.setContent('<h1>E2E page one</h1><div style="break-after: page"></div><h1>E2E page two</h1>');
  const pdfPath = test.info().outputPath('e2e.pdf');
  await pdfPage.pdf({ path: pdfPath, format: 'A4' });

  // Publisher uploads it (private by default) and shares it with the reader.
  const pub = await firstSignIn(browser, publisher);
  await pub.goto('/documents/upload');
  await expectAccessible(pub, 'upload');
  await pub.setInputFiles('#file', pdfPath);
  await pub.fill('#title', `E2E ${run}`);
  await pub.click('button[type=submit]');
  await expect(pub).toHaveURL(/\/documents\/[0-9a-f-]+\/manage/, { timeout: 60_000 });
  const documentId = /\/documents\/([0-9a-f-]+)\/manage/.exec(pub.url())![1];
  await pub.fill('input[name=shareWith]', reader.username);
  await pub.click('form.inline button[type=submit]');
  await expect(pub.getByText(`Shared with ${reader.username}.`)).toBeVisible();
  await expectAccessible(pub, 'manage');

  // The reader sees every tile load, and can turn pages with the keyboard.
  const rd = await firstSignIn(browser, reader);
  await expect(rd.getByText(`E2E ${run}`)).toBeVisible();
  await expectAccessible(rd, 'document list');
  await rd.goto(`/viewer/${documentId}`);
  await expect(rd.locator('.tile').first()).toBeVisible();
  await expect(rd.locator('.tile.pending')).toHaveCount(0, { timeout: 30_000 });
  await expectAccessible(rd, 'viewer');
  await rd.keyboard.press('ArrowRight');
  await expect(rd).toHaveURL(/[?&]page=2/);
  await expect(rd.locator('#page-input')).toHaveValue('2');
  await expect(rd.locator('.tile.pending')).toHaveCount(0, { timeout: 30_000 });

  // Someone it wasn't shared with can't tell it exists.
  const out = await firstSignIn(browser, outsider);
  await expect(out.getByText(`E2E ${run}`)).toHaveCount(0);
  await out.goto(`/viewer/${documentId}`);
  await expect(out.getByText(/hasn.t been shared with you/)).toBeVisible();
});

let adminPage: Page | undefined;

// Leave nothing active behind: the users this run created are disabled (never
// point this suite at production; it creates accounts).
test.afterAll(async () => {
  if (!adminPage) {
    return;
  }
  const csrf = (await adminPage.context().cookies()).find((c) => c.name === 'XSRF-TOKEN')?.value ?? '';
  for (const user of [publisher, reader, outsider]) {
    await adminPage.request.patch(`/api/admin/users/${user.username}`, {
      data: { enabled: false }, headers: { 'X-XSRF-TOKEN': csrf },
    });
  }
});

/** POSTs to the API with the double-submit CSRF header, as the SPA does. */
async function postJson(api: APIRequestContext, url: string, body: unknown, headers: Record<string, string> = {}) {
  const csrf = (await api.storageState()).cookies.find((c) => c.name === 'XSRF-TOKEN')?.value ?? '';
  return api.post(url, { data: body, headers: { 'X-XSRF-TOKEN': csrf, ...headers } });
}

test('a spoofed X-Forwarded-For header cannot reset the sign-in lockout', async ({ baseURL }) => {
  // Regression: the proxy used to append to a client-supplied X-Forwarded-For,
  // and the backend trusted it, so changing the header on every attempt gave
  // unlimited password guesses. nginx must overwrite it with the real peer.
  const api = await request.newContext({ baseURL });
  await api.get('/api/auth/me'); // picks up the CSRF cookie
  const username = `e2e-spoof-${run}`;
  const statuses: number[] = [];
  for (let attempt = 0; attempt < 6; attempt++) {
    const response = await postJson(api, '/api/auth/login', { username, password: `wrong-${attempt}` },
      { 'X-Forwarded-For': `203.0.113.${attempt + 1}` });
    statuses.push(response.status());
  }
  expect(statuses.slice(0, 5)).toEqual([401, 401, 401, 401, 401]);
  expect(statuses[5]).toBe(429);
  await api.dispose();
});
