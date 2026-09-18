import { Browser, Page, expect, test } from '@playwright/test';

const ADMIN_USER = process.env['E2E_ADMIN_USER'] ?? 'admin';
const ADMIN_PASSWORD = process.env['E2E_ADMIN_PASSWORD'] ?? '';

// Unique per run, so the suite can be re-run against the same database.
const run = Date.now().toString(36);
const publisher = { username: `e2e-pub-${run}`, password: 'e2e-publisher-password', role: 'PUBLISHER' };
const reader = { username: `e2e-reader-${run}`, password: 'e2e-reader-password', role: 'READER' };
const outsider = { username: `e2e-outsider-${run}`, password: 'e2e-outsider-password', role: 'READER' };

async function signIn(browser: Browser, username: string, password: string): Promise<Page> {
  const page = await (await browser.newContext()).newPage();
  await page.goto('/login');
  await page.fill('#username', username);
  await page.fill('#password', password);
  await page.click('button[type=submit]');
  await expect(page).toHaveURL(/\/documents/);
  return page;
}

test('a publisher shares a document with one reader, and nobody else can see it', async ({ browser }) => {
  test.skip(!ADMIN_PASSWORD, 'Set E2E_ADMIN_PASSWORD to run end-to-end tests');

  // Admin creates the accounts (there is no self-signup).
  const admin = await signIn(browser, ADMIN_USER, ADMIN_PASSWORD);
  await admin.goto('/admin');
  for (const user of [publisher, reader, outsider]) {
    await admin.fill('input[name=newUsername]', user.username);
    await admin.fill('input[name=newPassword]', user.password);
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
  const pub = await signIn(browser, publisher.username, publisher.password);
  await pub.goto('/documents/upload');
  await pub.setInputFiles('#file', pdfPath);
  await pub.fill('#title', `E2E ${run}`);
  await pub.click('button[type=submit]');
  await expect(pub).toHaveURL(/\/documents\/[0-9a-f-]+\/manage/, { timeout: 60_000 });
  const documentId = /\/documents\/([0-9a-f-]+)\/manage/.exec(pub.url())![1];
  await pub.fill('input[name=shareWith]', reader.username);
  await pub.click('form.inline button[type=submit]');
  await expect(pub.getByText(`Shared with ${reader.username}.`)).toBeVisible();

  // The reader sees every tile load, and can turn pages with the keyboard.
  const rd = await signIn(browser, reader.username, reader.password);
  await rd.goto(`/viewer/${documentId}`);
  await expect(rd.locator('.tile').first()).toBeVisible();
  await expect(rd.locator('.tile.pending')).toHaveCount(0, { timeout: 30_000 });
  await rd.keyboard.press('ArrowRight');
  await expect(rd).toHaveURL(/[?&]page=2/);
  await expect(rd.locator('#page-input')).toHaveValue('2');
  await expect(rd.locator('.tile.pending')).toHaveCount(0, { timeout: 30_000 });

  // Someone it wasn't shared with can't tell it exists.
  const out = await signIn(browser, outsider.username, outsider.password);
  await expect(out.getByText(`E2E ${run}`)).toHaveCount(0);
  await out.goto(`/viewer/${documentId}`);
  await expect(out.getByText(/hasn.t been shared with you/)).toBeVisible();
});
