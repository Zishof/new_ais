# Phase 7 account-group UAT runbook

This runbook validates the data-minimized, read-only account-group directory. Run it only against
an isolated clone. Never add UAT users, roles, menu grants, or finance data to `ais` or
`streaming_ais`.

## Preflight

1. Record the row count and one-way schema/projection fingerprints for `akunting.grup_akun` in the
   source and clone using PostgreSQL sessions with `default_transaction_read_only=on`.
2. Confirm source CORE/FILE descriptors are read-only and the UAT CORE descriptor names the clone.
3. Confirm both account-group routes are `LEGACY`/`LEGACY_WRITE` for `local`. Promote only the UAT
   copies to `NEXT`/`NEXT_READ_ONLY`.
4. Supply database credentials and the handoff signing key only through process environment.
5. Record the UAT user's complete original role tuple before adding a negative-test role.

## Authorization fixture

Positive access requires both `job_has_menu(role, 36332)` and a matching `_read=1`
`role_privilage` row for an active assigned role. Prefer an existing effective role in the clone;
do not create a new positive grant merely for the test.

For the negative check, temporarily assign the UAT user one already-existing active role without
effective menu 36332. Change only the exact recorded role column. Restore the original tuple in a
cleanup transaction and do not alter any source role or menu row.

## Required checks

1. Complete a fresh one-time handoff and reach `/dashboard`, then `/finance/account-groups`.
2. Require the API to return seven rows and only `accountGroupId`, `name`, and `description` for
   each row.
3. Verify deterministic paging, literal case-insensitive name filtering, `%`, `_`, and backslash
   handling, invalid bounds, and both HTML/API no-menu 403 responses.
4. Require the normal `local` route to remain HTTP 404 while the UAT route is Next-owned.
5. Run desktop and mobile Playwright projects with axe WCAG A/AA scans.
6. Recount and fingerprint `akunting.grup_akun` after browser testing. Do not test a finance write.

## Route rollback

With one authenticated UAT session returning HTTP 200, set only the two UAT account-group routes
to `LEGACY`/`LEGACY_WRITE` and require HTTP 404. Restore them to `NEXT`/`NEXT_READ_ONLY` and require
HTTP 200 without restarting AIS Next. Reconfirm the local routes were not changed.

## Recovery rehearsal

Temporarily point only the UAT CORE descriptor to an unused localhost port. Keep the exact original
URL in memory and restore it from a `finally` path. After the short UAT idle-pool TTL expires,
require HTTP 503 with Problem Details type
`urn:ais-next:problem:account-groups-unavailable` and no JDBC URL, port, credential, or driver
detail. Restore the clone URL and require HTTP 200 on the same application PID.

Never stop PostgreSQL, mutate a finance relation, or point the UAT descriptor at a source database
to simulate recovery.

## Cleanup

Restore the exact UAT user role tuple. Require unchanged source role state, matching source/clone
fingerprints, the intended final route decisions, health `UP`, and the same application PID.
Perform one fresh positive handoff after cleanup to prove the retained role still reaches the
dashboard and account-group page. Retain only sanitized screenshots and evidence metadata.
