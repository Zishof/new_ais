import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';
import { createHmac, randomUUID } from 'node:crypto';

function handoffUrl(userId = 'admin', activeRoleId = process.env.AIS_E2E_ROLE ?? 'amp'): string | undefined {
  const signingKey = process.env.AIS_E2E_HANDOFF_SIGNING_KEY;
  if (!signingKey) return undefined;
  const encode = (value: string) => Buffer.from(value, 'utf8').toString('base64url');
  const payload = [
    'v1', encode('ais-legacy'), encode('ais-next'), encode(process.env.AIS_E2E_TENANT ?? 'uat-local'),
    encode(userId), encode(activeRoleId), encode(randomUUID().replaceAll('-', '')),
    String(Math.floor(Date.now() / 1000) + 300)
  ].join('.');
  const signature = createHmac('sha256', signingKey).update(payload).digest('base64url');
  return `/auth/handoff?token=${encodeURIComponent(`${payload}.${signature}`)}`;
}

test.beforeEach(async ({ page }) => {
  test.skip(process.env.AIS_E2E_SCHOOL_TYPES !== 'true', 'Enable only for the isolated writable UAT clone');
  const url = handoffUrl();
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the UAT signing key');
  await page.goto(url!);
  await expect(page).toHaveURL(/\/dashboard$/);
});

test('login reaches dashboard and accessible school-type catalogue', async ({ page }) => {
  await page.goto('/school-types');
  await expect(page.getByRole('heading', { name: 'Jenis sekolah', exact: true })).toBeVisible();
  await expect(page.locator('tbody tr')).toHaveCount(6);
  await expect(page.getByRole('link', { name: 'Tambah jenis sekolah' })).toBeVisible();
  const results = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa']).analyze();
  expect(results.violations).toEqual([]);
});

test('API supports filter, sort, optimistic CRUD, and cleanup', async ({ page }, testInfo) => {
  await page.goto('/school-types');
  const csrf = await page.locator('input[name="_csrf"]').first().getAttribute('value');
  expect(csrf).toBeTruthy();
  const mutationHeaders = { 'X-CSRF-TOKEN': csrf! };

  const filtered = await page.request.get('/api/v1/school-types?q=sm&sort=NAME_DESC&page=0&size=2');
  expect(filtered.status()).toBe(200);
  expect(await filtered.json()).toMatchObject({ page: 0, size: 2, total: 4 });

  const name = `UAT ${testInfo.project.name} ${randomUUID().slice(0, 8)}`;
  const createdResponse = await page.request.post('/api/v1/school-types', {
    headers: mutationHeaders,
    data: { name, levelId: 33, description: 'Playwright UAT', active: true }
  });
  expect(createdResponse.status()).toBe(201);
  const created = await createdResponse.json();
  const createEtag = createdResponse.headers().etag;
  expect(createEtag).toBeTruthy();

  const updatedResponse = await page.request.put(`/api/v1/school-types/${created.id}`, {
    headers: { ...mutationHeaders, 'If-Match': createEtag },
    data: { name: `${name} updated`, levelId: 32, description: 'Updated', active: false }
  });
  expect(updatedResponse.status()).toBe(200);
  expect(await updatedResponse.json()).toMatchObject({ active: false, levelId: 32 });
  const updateEtag = updatedResponse.headers().etag;

  const staleResponse = await page.request.put(`/api/v1/school-types/${created.id}`, {
    headers: { ...mutationHeaders, 'If-Match': createEtag },
    data: { name, levelId: 33, description: 'Stale', active: true }
  });
  expect(staleResponse.status()).toBe(409);

  const deletedResponse = await page.request.delete(`/api/v1/school-types/${created.id}`, {
    headers: { ...mutationHeaders, 'If-Match': updateEtag }
  });
  expect(deletedResponse.status()).toBe(204);
});

test('Excel export is bounded and the production-like local tenant remains closed', async ({ page }) => {
  const exportResponse = await page.request.get('/api/v1/school-types/export.xlsx');
  expect(exportResponse.status()).toBe(200);
  expect(exportResponse.headers()['content-type']).toContain(
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
  expect((await exportResponse.body()).byteLength).toBeLessThan(1024 * 1024);

  const localResponse = await page.request.get('http://localhost:8081/school-types');
  expect(localResponse.status()).toBe(404);
});

test('role without menu 881247 is denied by server authorization', async ({ page }) => {
  const unauthorizedUrl = handoffUrl('uat_dosen', 'Dosen');
  expect(unauthorizedUrl).toBeTruthy();
  await page.goto(unauthorizedUrl!);
  await expect(page).toHaveURL(/\/dashboard$/);

  const response = await page.request.get('/school-types');
  expect(response.status()).toBe(403);
});
