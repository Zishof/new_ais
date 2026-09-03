import { expect, test } from '@playwright/test';
import { createHmac, randomUUID } from 'node:crypto';

function handoffUrl(): string | undefined {
  const signingKey = process.env.AIS_E2E_HANDOFF_SIGNING_KEY;
  if (!signingKey) return undefined;
  const encode = (value: string) => Buffer.from(value, 'utf8').toString('base64url');
  const payload = [
    'v1', encode('ais-legacy'), encode('ais-next'), encode('local'),
    encode('admin'), encode('am'), encode(randomUUID().replaceAll('-', '')),
    String(Math.floor(Date.now() / 1000) + 300)
  ].join('.');
  const signature = createHmac('sha256', signingKey).update(payload).digest('base64url');
  return `/auth/handoff?token=${encodeURIComponent(`${payload}.${signature}`)}`;
}

function percentile(values: number[], percentileRank: number): number {
  const sorted = [...values].sort((left, right) => left - right);
  return sorted[Math.max(0, Math.ceil(percentileRank * sorted.length) - 1)];
}

test('local authenticated read path stays inside initial performance budgets', async ({ page }, testInfo) => {
  const url = handoffUrl();
  test.skip(!url, 'Set AIS_E2E_HANDOFF_SIGNING_KEY to the local test signing key');
  await page.goto(url!);

  for (let index = 0; index < 5; index += 1) {
    const warmup = await page.request.get('/api/v1/roles?page=0&size=20');
    expect(warmup.status()).toBe(200);
  }

  const apiDurations: number[] = [];
  let payloadBytes = 0;
  for (let index = 0; index < 40; index += 1) {
    const startedAt = performance.now();
    const response = await page.request.get('/api/v1/roles?page=0&size=20');
    apiDurations.push(performance.now() - startedAt);
    expect(response.status()).toBe(200);
    if (index === 0) payloadBytes = (await response.body()).byteLength;
  }

  const htmlDurations: number[] = [];
  for (let index = 0; index < 15; index += 1) {
    const startedAt = performance.now();
    const response = await page.goto('/roles?page=0&size=20');
    htmlDurations.push(performance.now() - startedAt);
    expect(response?.status()).toBe(200);
  }

  const evidence = {
    project: testInfo.project.name,
    apiSamples: apiDurations.length,
    apiP50Ms: Math.round(percentile(apiDurations, 0.50)),
    apiP95Ms: Math.round(percentile(apiDurations, 0.95)),
    apiP99Ms: Math.round(percentile(apiDurations, 0.99)),
    htmlSamples: htmlDurations.length,
    htmlP95Ms: Math.round(percentile(htmlDurations, 0.95)),
    rolePayloadBytes: payloadBytes
  };
  console.log(`PERF_EVIDENCE ${JSON.stringify(evidence)}`);

  expect(evidence.apiP95Ms).toBeLessThanOrEqual(500);
  expect(evidence.apiP99Ms).toBeLessThanOrEqual(1_000);
  expect(evidence.htmlP95Ms).toBeLessThanOrEqual(750);
  expect(evidence.rolePayloadBytes).toBeLessThanOrEqual(100 * 1_024);
});
