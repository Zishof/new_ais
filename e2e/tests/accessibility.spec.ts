import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';
import { createHmac, randomUUID } from 'node:crypto';

function handoffUrl(userId = 'admin', activeRoleId = process.env.AIS_E2E_ROLE ?? 'am'): string | undefined {
  const signingKey = process.env.AIS_E2E_HANDOFF_SIGNING_KEY;
  if (!signingKey) return undefined;
  const encode = (value: string) => Buffer.from(value, 'utf8').toString('base64url');
  const payload = [
    'v1', encode('ais-legacy'), encode('ais-next'), encode(process.env.AIS_E2E_TENANT ?? 'local'),
    encode(userId), encode(activeRoleId), encode(randomUUID().replaceAll('-', '')),
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

test('authenticated profile remains credential-free and accessible', async ({ page }) => {
  const url = handoffUrl();
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the local test signing key');
  await page.goto(url!);
  await page.goto('/profile');
  await expect(page.getByRole('heading', { name: 'Profil pengguna' })).toBeVisible();
  await expect(page.getByRole('code')).toHaveText('admin');
  await expect(page.locator('dd').filter({ hasText: new RegExp(`^${process.env.AIS_E2E_ROLE ?? 'am'}$`) }).first()).toBeVisible();
  await expect(page.getByText(/tidak memuat password/i)).toBeVisible();
  const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa']).analyze();
  expect(results.violations).toEqual([]);
});

test('global search returns authorized role results and accessible markup', async ({ page }) => {
  const url = handoffUrl();
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the local test signing key');
  await page.goto(url!);
  await page.goto('/search?q=Admin');
  await expect(page.getByRole('heading', { name: 'Pencarian global' })).toBeVisible();
  await expect(page.locator('.list-group-item').first()).toBeVisible();
  const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa']).analyze();
  expect(results.violations).toEqual([]);
});

test('versioned identity APIs expose authorized read-only projections', async ({ page }) => {
  const url = handoffUrl();
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the local test signing key');
  await page.goto(url!);

  const profile = await page.request.get('/api/v1/profile');
  expect(profile.status()).toBe(200);
  expect(await profile.json()).toMatchObject({ userId: 'admin', active: true });

  const search = await page.request.get('/api/v1/search?q=Admin');
  expect(search.status()).toBe(200);
  expect(await search.json()).toEqual(expect.arrayContaining([
    expect.objectContaining({ type: 'Grup pengguna' })
  ]));

  const roles = await page.request.get('/api/v1/roles?page=0&size=5');
  expect(roles.status()).toBe(200);
  expect(await roles.json()).toMatchObject({ page: 0, size: 5 });
});

test('role directory denies an authenticated role without menu read privilege', async ({ page }) => {
  const url = handoffUrl('uat_dosen', 'Dosen');
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the local test signing key');
  await page.goto(url!);

  const response = await page.request.get('/roles');
  expect(response.status()).toBe(403);
});

test('handoff rejects a role that is not assigned to the signed user', async ({ page }) => {
  const url = handoffUrl('admin', 'Dosen');
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the local test signing key');

  const response = await page.goto(url!);
  expect(response?.status()).toBe(401);
});

test('handoff token cannot be replayed after successful consumption', async ({ page }) => {
  const url = handoffUrl();
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the local test signing key');
  await page.goto(url!);
  await expect(page).toHaveURL(/\/dashboard$/);

  const replay = await page.goto(url!);
  expect(replay?.status()).toBe(401);
});
