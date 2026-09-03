import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';
import { createHmac, randomUUID } from 'node:crypto';

/**
 * Builds a one-time UAT handoff URL without persisting its signing key or token.
 *
 * @param activeRoleId exact legacy role selected for the resulting session
 * @returns relative handoff URL, or undefined when the runtime key is absent
 */
function handoffUrl(activeRoleId: string): string | undefined {
  const signingKey = process.env.AIS_E2E_HANDOFF_SIGNING_KEY;
  if (!signingKey) return undefined;
  const encode = (value: string) => Buffer.from(value, 'utf8').toString('base64url');
  const payload = [
    'v1', encode('ais-legacy'), encode('ais-next'), encode(process.env.AIS_E2E_TENANT ?? 'uat-local'),
    encode(process.env.AIS_E2E_USER ?? 'aisnext_uat'), encode(activeRoleId),
    encode(randomUUID().replaceAll('-', '')), String(Math.floor(Date.now() / 1000) + 300)
  ].join('.');
  const signature = createHmac('sha256', signingKey).update(payload).digest('base64url');
  return `/auth/handoff?token=${encodeURIComponent(`${payload}.${signature}`)}`;
}

test.beforeEach(async ({ page }) => {
  test.skip(process.env.AIS_E2E_ACCOUNT_GROUPS !== 'true',
    'Enable only for the isolated account-group UAT clone');
  const url = handoffUrl(process.env.AIS_E2E_ACCOUNT_GROUP_ROLE ?? 'amp');
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the UAT signing key');
  await page.goto(url!);
  await expect(page).toHaveURL(/\/dashboard$/);
});

test('login reaches accessible read-only account-group directory', async ({ page }, testInfo) => {
  const response = await page.request.get('/api/v1/finance/account-groups');
  expect(response.status()).toBe(200);
  const result = await response.json();
  expect(result).toMatchObject({ page: 0, size: 25, total: 7 });

  await page.goto('/finance/account-groups');
  await expect(page.getByRole('heading', { name: 'Grup akun keuangan' })).toBeVisible();
  await expect(page.locator('tbody tr').first().getByRole(
    'cell', { name: result.items[0].name, exact: true }).first()).toBeVisible();
  await expect(page.getByRole('status')).toContainText(
    'Semua perubahan, jurnal, posting, dan pembayaran tetap dilakukan di AIS Legacy.');
  const accessibility = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(accessibility.violations).toEqual([]);
  await testInfo.attach('account-groups', {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png'
  });
});

test('API minimizes fields and keeps literal filter and paging deterministic', async ({ page }) => {
  const allResponse = await page.request.get('/api/v1/finance/account-groups?size=100');
  expect(allResponse.status()).toBe(200);
  const all = await allResponse.json();
  expect(all.items).toHaveLength(7);
  expect(Object.keys(all.items[0]).sort()).toEqual(['accountGroupId', 'description', 'name']);

  const firstPage = await page.request.get('/api/v1/finance/account-groups?page=0&size=1');
  const secondPage = await page.request.get('/api/v1/finance/account-groups?page=1&size=1');
  expect((await firstPage.json()).items[0].accountGroupId).toBe(all.items[0].accountGroupId);
  expect((await secondPage.json()).items[0].accountGroupId).toBe(all.items[1].accountGroupId);

  const name = all.items[1].name as string;
  const filter = name.length > 2 ? name.slice(1, 3) : name;
  const filtered = await page.request.get(
    `/api/v1/finance/account-groups?q=${encodeURIComponent(filter)}`);
  expect((await filtered.json()).items.map(
    (accountGroup: { accountGroupId: number }) => accountGroup.accountGroupId))
    .toContain(all.items[1].accountGroupId);

  const wildcard = await page.request.get('/api/v1/finance/account-groups?q=%25_%5C');
  expect(wildcard.status()).toBe(200);
  expect((await wildcard.json()).total).toBe(0);
});

test('invalid bounds, exact authority, and local routing fail closed', async ({ page }) => {
  const oversized = await page.request.get('/api/v1/finance/account-groups?size=101');
  expect(oversized.status()).toBe(400);

  const unauthorizedUrl = handoffUrl('Dosen');
  expect(unauthorizedUrl).toBeTruthy();
  await page.goto(unauthorizedUrl!);
  await expect(page).toHaveURL(/\/dashboard$/);
  expect((await page.request.get('/finance/account-groups')).status()).toBe(403);
  expect((await page.request.get('/api/v1/finance/account-groups')).status()).toBe(403);

  const local = await page.request.get('http://localhost:8081/finance/account-groups');
  expect(local.status()).toBe(404);
});
