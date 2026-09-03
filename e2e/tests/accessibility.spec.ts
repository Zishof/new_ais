import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';
import { createHmac, randomUUID } from 'node:crypto';

function handoffUrl(): string | undefined {
  const signingKey = process.env.AIS_E2E_HANDOFF_SIGNING_KEY;
  if (!signingKey) return undefined;
  const encode = (value: string) => Buffer.from(value, 'utf8').toString('base64url');
  const payload = [
    'v1', encode('ais-legacy'), encode('ais-next'), encode('local'),
    encode('playwright-user'), encode('ADMIN'), encode(randomUUID().replaceAll('-', '')),
    String(Math.floor(Date.now() / 1000) + 300)
  ].join('.');
  const signature = createHmac('sha256', signingKey).update(payload).digest('base64url');
  return `/auth/handoff?token=${encodeURIComponent(`${payload}.${signature}`)}`;
}

test('public landing is responsive and has no automatically detectable WCAG A/AA violations', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'AIS Next' })).toBeVisible();
  const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa']).analyze();
  expect(results.violations).toEqual([]);
});

test('authenticated role directory reads legacy data', async ({ page }) => {
  const url = handoffUrl();
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the local test signing key');
  await page.goto(url!);
  await page.goto('/roles');
  await expect(page.getByRole('heading', { name: 'Grup pengguna legacy' })).toBeVisible();
  await expect(page.locator('tbody tr').first()).toBeVisible();
  const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa']).analyze();
  expect(results.violations).toEqual([]);
});
