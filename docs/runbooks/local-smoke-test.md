# Local smoke-test runbook

This runbook validates the actual localhost databases while keeping legacy schemas read-only.

## Preconditions

- Java 25 is active.
- `ais_next_control` exists and may be migrated by AIS Next.
- `ais` and `streaming_ais` exist. Use a least-privilege read account outside this operator-only test.
- Set `AIS_CONTROL_DB_USERNAME`, `AIS_CONTROL_DB_PASSWORD`, `AIS_LOCAL_DB_USERNAME`, `AIS_LOCAL_DB_PASSWORD`, and a random 32+ byte `AIS_HANDOFF_SIGNING_KEY` in the current process. Do not write them to a file.

## Procedure

1. Capture a legacy schema fingerprint/table and column counts in a read-only transaction.
2. Run `.\mvnw.cmd clean verify`.
3. Start `apps/ais-next-web` with the environment above.
4. Confirm `/actuator/health` is `UP` and the landing page loads for host `localhost`.
5. Issue a token with issuer `ais-legacy`, audience `ais-next`, tenant `local`, a test user/role, unique nonce and expiration under five minutes.
6. Call `/auth/handoff`, retaining the new session cookie. Confirm replaying the token fails.
7. Call `/api/v1/roles?page=0&size=25`; confirm total role count matches a direct read-only query (57 in the 3 September 2026 snapshot).
8. Set `AIS_E2E_HANDOFF_SIGNING_KEY` to the same local-only key used by the application, then run Playwright. Every desktop/mobile project creates its own fresh one-time token before opening `/roles` and running the accessibility scan.
9. Re-capture the legacy fingerprint/counts. They must be identical. Inspect `ais_next_control.flyway_schema_history` and control tables separately.
10. Stop the application and record command exit codes, artifact SHA-256 and gaps.

## Failure/rollback

Stop AIS Next, route traffic to legacy and preserve logs with tokens redacted. Never “repair” a mismatch by altering a legacy table. Restore only `ais_next_control` from its independent backup if its migration failed.
