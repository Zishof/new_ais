import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';
import { createHmac, randomUUID } from 'node:crypto';

/**
 * Builds one short-lived UAT handoff URL for a visual-shell browser session.
 *
 * @returns relative handoff URL, or undefined when the runtime signing key is absent
 */
function handoffUrl(): string | undefined {
  const signingKey = process.env.AIS_E2E_HANDOFF_SIGNING_KEY;
  if (!signingKey) return undefined;
  const encode = (value: string) => Buffer.from(value, 'utf8').toString('base64url');
  const payload = [
    'v1', encode('ais-legacy'), encode('ais-next'), encode(process.env.AIS_E2E_TENANT ?? 'uat-local'),
    encode(process.env.AIS_E2E_USER ?? 'aisnext_uat'), encode(process.env.AIS_E2E_ROLE ?? 'amp'),
    encode(randomUUID().replaceAll('-', '')), String(Math.floor(Date.now() / 1000) + 300)
  ].join('.');
  const signature = createHmac('sha256', signingKey).update(payload).digest('base64url');
  return `/auth/handoff?token=${encodeURIComponent(`${payload}.${signature}`)}`;
}

test.beforeEach(() => {
  test.skip(process.env.AIS_E2E_UI_SHELL !== 'true',
    'Enable only for the isolated AIS Next visual-shell UAT');
});

test('public entry is complete, accessible, and links back to Legacy login', async ({ page }, testInfo) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'AIS Next', exact: true })).toBeVisible();
  await expect(page.getByRole('heading', {
    name: 'Satu ruang kerja untuk layanan pendidikan.'
  })).toBeVisible();
  await expect(page.getByRole('link', { name: /Masuk melalui AIS Legacy/ }))
    .toHaveAttribute('href', process.env.AIS_E2E_LEGACY_LOGIN_URL ?? 'http://localhost:18080/ais/login');
  const accessibility = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(accessibility.violations).toEqual([]);
  await testInfo.attach('public-entry', {
    body: await page.screenshot({ fullPage: true }), contentType: 'image/png'
  });
});

test('authenticated dashboard exposes role-scoped modules and responsive navigation', async ({ page }, testInfo) => {
  const url = handoffUrl();
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the UAT signing key');
  await page.goto(url!);
  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByRole('heading', { name: /Selamat datang/ })).toBeVisible();
  await expect(page.getByRole('region', { name: 'Daftar layanan AIS Next' })).toBeVisible();
  await expect(page.getByRole('link', { name: /Grup akun/ }).last()).toBeVisible();

  const viewport = page.viewportSize();
  if (viewport && viewport.width < 992) {
    await page.getByRole('button', { name: 'Buka atau tutup menu utama' }).click();
  }
  await expect(page.getByRole('navigation', { name: 'Navigasi utama' })).toBeVisible();
  await expect(page.getByRole('navigation', { name: 'Navigasi utama' })
    .getByRole('link', { name: 'Dasbor' })).toHaveAttribute('aria-current', 'page');

  const accessibility = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(accessibility.violations).toEqual([]);
  await testInfo.attach('dashboard', {
    body: await page.screenshot({ fullPage: true }), contentType: 'image/png'
  });
});

test('data page keeps active navigation, readable table, and safe mobile overflow', async ({ page }, testInfo) => {
  const url = handoffUrl();
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the UAT signing key');
  await page.goto(url!);
  await page.goto('/finance/account-groups');
  await expect(page.getByRole('heading', { name: 'Grup akun keuangan' })).toBeVisible();
  await expect(page.getByRole('region', { name: 'Tabel grup akun keuangan' })).toBeVisible();
  await expect(page.locator('[data-ais-route="/finance"]')).toHaveAttribute('aria-current', 'page');

  const viewport = page.viewportSize();
  if (viewport && viewport.width < 992) {
    const overflow = await page.getByRole('region', { name: 'Tabel grup akun keuangan' })
      .evaluate((element) => element.scrollWidth > element.clientWidth);
    expect(overflow).toBe(true);
  }

  const accessibility = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(accessibility.violations).toEqual([]);
  await testInfo.attach('account-groups-shell', {
    body: await page.screenshot({ fullPage: true }), contentType: 'image/png'
  });
});
