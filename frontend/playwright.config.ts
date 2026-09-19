import { defineConfig, devices } from '@playwright/test';

/**
 * End-to-end tests against a running full stack (`docker compose --profile full up -d --build`,
 * served at http://localhost:8081). Needs an admin account: E2E_ADMIN_USER / E2E_ADMIN_PASSWORD.
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 120_000,
  retries: process.env['CI'] ? 1 : 0,
  reporter: process.env['CI'] ? [['list'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: process.env['E2E_BASE_URL'] ?? 'http://localhost:8081',
    trace: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
});
