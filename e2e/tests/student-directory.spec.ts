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
  test.skip(process.env.AIS_E2E_STUDENTS !== 'true', 'Enable only for the isolated student UAT clone');
  const url = handoffUrl(process.env.AIS_E2E_STUDENT_ROLE ?? 'amp');
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the UAT signing key');
  await page.goto(url!);
  await expect(page).toHaveURL(/\/dashboard$/);
});

test('login reaches accessible role-scoped student directory', async ({ page }, testInfo) => {
  const response = await page.request.get('/api/v1/academic/students');
  expect(response.status()).toBe(200);
  const result = await response.json();
  expect(result).toMatchObject({ page: 0, size: 25, total: 3 });

  await page.goto('/academic/students');
  await expect(page.getByRole('heading', { name: 'Direktori siswa sekolah' })).toBeVisible();
  await expect(page.getByText(result.items[0].studentName, { exact: true })).toBeVisible();
  await expect(page.getByText('Proyeksi baca-saja')).toBeVisible();
  const accessibility = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(accessibility.violations).toEqual([]);
  await testInfo.attach('student-directory', {
    body: await page.screenshot({ fullPage: true }),
    contentType: 'image/png'
  });
});

test('API minimizes fields and keeps literal filter and paging deterministic', async ({ page }) => {
  const allResponse = await page.request.get('/api/v1/academic/students');
  const all = await allResponse.json();
  expect(all.items).toHaveLength(3);
  expect(Object.keys(all.items[0]).sort()).toEqual([
    'active', 'currentClassName', 'entryYear', 'exitStatusName', 'initialStatusName',
    'schoolName', 'studentId', 'studentName', 'studentNumber'
  ]);
  expect(all.items.every((student: { active: boolean }) => student.active)).toBe(true);

  const firstPage = await page.request.get('/api/v1/academic/students?page=0&size=1');
  const secondPage = await page.request.get('/api/v1/academic/students?page=1&size=1');
  expect((await firstPage.json()).items[0].studentId).toBe(all.items[0].studentId);
  expect((await secondPage.json()).items[0].studentId).toBe(all.items[1].studentId);

  const number = all.items[1].studentNumber as string;
  const filter = number.length > 2 ? number.slice(1, 3) : number;
  const filtered = await page.request.get(`/api/v1/academic/students?q=${encodeURIComponent(filter)}`);
  expect((await filtered.json()).items.map((student: { studentId: number }) => student.studentId))
    .toContain(all.items[1].studentId);

  const wildcard = await page.request.get('/api/v1/academic/students?q=%25_%5C');
  expect(wildcard.status()).toBe(200);
  expect((await wildcard.json()).total).toBe(0);
});

test('role scope, invalid bounds, exact authority, and local routing fail closed', async ({ page }) => {
  const scopedUrl = handoffUrl('am');
  expect(scopedUrl).toBeTruthy();
  await page.goto(scopedUrl!);
  await expect(page).toHaveURL(/\/dashboard$/);
  const scoped = await page.request.get('/api/v1/academic/students');
  expect(scoped.status()).toBe(200);
  expect((await scoped.json()).total).toBe(0);

  const oversized = await page.request.get('/api/v1/academic/students?size=101');
  expect(oversized.status()).toBe(400);

  const unauthorizedUrl = handoffUrl('Dosen');
  expect(unauthorizedUrl).toBeTruthy();
  await page.goto(unauthorizedUrl!);
  await expect(page).toHaveURL(/\/dashboard$/);
  expect((await page.request.get('/academic/students')).status()).toBe(403);

  const local = await page.request.get('http://localhost:8081/academic/students');
  expect(local.status()).toBe(404);
});
