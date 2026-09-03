import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';
import { createHmac, randomUUID } from 'node:crypto';

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
  test.skip(process.env.AIS_E2E_ATTENDANCE !== 'true', 'Enable only for the isolated attendance UAT clone');
  const url = handoffUrl(process.env.AIS_E2E_ATTENDANCE_ROLE ?? 'am');
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the UAT signing key');
  await page.goto(url!);
  await expect(page).toHaveURL(/\/dashboard$/);
});

test('login reaches accessible daily attendance monitor', async ({ page }, testInfo) => {
  await page.goto('/attendance/daily?date=2026-09-04');
  await expect(page.getByRole('heading', { name: 'Kehadiran pegawai harian' })).toBeVisible();
  await expect(page.getByText('UAT Attendance Alpha')).toBeVisible();
  await expect(page.getByText('UAT Attendance Gamma')).toBeVisible();
  await expect(page.getByText('Monitor baca-saja')).toBeVisible();
  const results = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(results.violations).toEqual([]);
  await testInfo.attach('daily-attendance', {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png'
  });
});

test('API pages and filters deterministic recorded state', async ({ page }) => {
  const allResponse = await page.request.get(
    '/api/v1/attendance/daily?date=2026-09-04&recordState=ALL&size=25');
  expect(allResponse.status()).toBe(200);
  const all = await allResponse.json();
  expect(all).toMatchObject({ page: 0, size: 25, total: 3 });
  expect(all.items.find((item: { employeeNumber: string }) => item.employeeNumber === 'UAT-001'))
    .toMatchObject({ attendanceId: -904101, note: 'latest duplicate', recordState: 'RECORDED' });

  const recorded = await page.request.get(
    '/api/v1/attendance/daily?date=2026-09-04&recordState=RECORDED');
  expect(await recorded.json()).toMatchObject({ total: 2 });
  const unrecorded = await page.request.get(
    '/api/v1/attendance/daily?date=2026-09-04&recordState=UNRECORDED');
  expect(await unrecorded.json()).toMatchObject({ total: 1 });

  const secondPage = await page.request.get(
    '/api/v1/attendance/daily?date=2026-09-04&page=1&size=1');
  expect(await secondPage.json()).toMatchObject({
    page: 1,
    items: [{ employeeNumber: 'UAT-002' }]
  });
});

test('invalid input and role without menu read are denied server-side', async ({ page }) => {
  const invalid = await page.request.get(
    '/api/v1/attendance/daily?date=2026-09-04&recordState=INVALID');
  expect(invalid.status()).toBe(400);
  const oversized = await page.request.get(
    '/api/v1/attendance/daily?date=2026-09-04&size=101');
  expect(oversized.status()).toBe(400);

  const unauthorizedUrl = handoffUrl('amp');
  expect(unauthorizedUrl).toBeTruthy();
  await page.goto(unauthorizedUrl!);
  await expect(page).toHaveURL(/\/dashboard$/);
  const denied = await page.request.get('/attendance/daily?date=2026-09-04');
  expect(denied.status()).toBe(403);

  const local = await page.request.get('http://localhost:8081/attendance/daily?date=2026-09-04');
  expect(local.status()).toBe(404);
});
