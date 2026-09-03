# Phase 6 library item-type UAT runbook

This runbook validates the data-minimized, read-only library item-type directory. Run it only
against an isolated clone. Never add UAT users, roles, or menu grants to `ais` or `streaming_ais`.

## Preflight

1. Record the row count and one-way schema/projection fingerprints for `library.jenis_item` in the
   source and clone using PostgreSQL sessions with `default_transaction_read_only=on`.
2. Confirm source CORE/FILE descriptors are read-only and the UAT CORE descriptor names the clone.
3. Confirm both item-type routes are `LEGACY`/`LEGACY_WRITE` for `local`. Promote only the UAT
   copies to `NEXT`/`NEXT_READ_ONLY`.
4. Supply database credentials and the handoff signing key only through process environment.
5. Record the UAT user's complete original role tuple before adding a negative-test role.

## Authorization fixture

Positive access requires both `job_has_menu(role, 56141)` and a matching `_read=1`
`role_privilage` row for an active assigned role. Prefer an existing effective role in the clone;
do not create a new positive grant merely for the test.

For the negative check, temporarily assign the UAT user one already-existing active role without
effective menu 56141. Change only the exact recorded role column. Restore the original tuple in a
single cleanup transaction and do not alter any source role or menu row.

## Required checks

1. Complete a fresh one-time handoff and reach `/dashboard`, then
   `/supporting/library/item-types`.
2. Require the API to return 31 rows across its pages and only `itemTypeId`, `name`, and
   `description` for each row.
3. Verify deterministic paging, name-or-description filtering, literal `%`, `_`, and backslash
   handling, invalid bounds, and both HTML/API no-menu 403 responses.
4. Require the normal `local` route to remain HTTP 404 while the UAT route is Next-owned.
5. Run desktop and mobile Playwright projects with axe WCAG A/AA scans.
6. Recount and fingerprint `library.jenis_item` after browser testing.

## Route rollback

With one authenticated UAT session returning HTTP 200, set only the two UAT item-type routes to
`LEGACY`/`LEGACY_WRITE` and require HTTP 404. Restore them to `NEXT`/`NEXT_READ_ONLY` and require
HTTP 200 without restarting AIS Next. Reconfirm the local routes were not changed.

## Recovery rehearsal

Temporarily point only the UAT CORE descriptor to an unused localhost port. Keep the exact original
URL in memory and restore it from a `finally` path. After the short UAT idle-pool TTL expires,
require HTTP 503 with Problem Details type
`urn:ais-next:problem:library-item-types-unavailable` and no JDBC URL, port, credential, or driver
detail. Restore the clone URL and require HTTP 200 on the same application PID.

Never stop PostgreSQL or point the UAT descriptor at a source database to simulate recovery.

## Cleanup

Restore the exact UAT user role tuple. Require unchanged source role state, matching source/clone
fingerprints, the intended final route decisions, health `UP`, and the same application PID.
Perform one fresh positive handoff after cleanup to prove the retained role still reaches the
dashboard and item-type page. Retain only sanitized screenshots and evidence metadata.
