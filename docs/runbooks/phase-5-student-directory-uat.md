# Phase 5 school-student directory UAT runbook

This runbook validates the data-minimized, read-only school-student directory. Run it only against
an isolated clone. Never add test users, role grants, or student rows to `ais` or `streaming_ais`.

## Preflight

1. Record counts and one-way fingerprints for the five audited `sekolah` relations in both the
   source and clone using PostgreSQL sessions with `default_transaction_read_only=on`.
2. Confirm the source CORE/FILE descriptors are read-only and that the UAT CORE descriptor names
   the isolated clone.
3. Confirm `/academic/students` and `/api/v1/academic/students` are `LEGACY`/`LEGACY_WRITE` for
   `local`. Promote only the UAT copies to `NEXT`/`NEXT_READ_ONLY`.
4. Supply database credentials and the handoff signing key only through process environment.
5. Record the UAT user's original role columns and every exact clone-only authorization fixture.

## Authorization fixture

Menu privilege is effective only when both legacy layers agree: `role_privilage` provides the
operation bits and `job_has_menu` assigns the menu to the selected role. Do not infer effective
access from either table alone.

When a negative scope check needs an otherwise-valid role that the clone does not assign to menu
887727, insert one exact clone-only `job_has_menu` row, record its composite key, and delete that
same row during cleanup. Do not change the source role catalog. Temporarily assign the UAT user
only the roles required by the test and retain the complete original role tuple.

## Required checks

1. Complete a fresh one-time handoff and reach `/dashboard`, then `/academic/students`.
2. Require the JSON response to contain only the approved contract fields and active rows.
3. Verify role foundation/school scope, deterministic paging, name/number filtering, literal `%`
   and `_` handling, invalid bounds, and an exact no-menu 403.
4. Require the normal `local` route to remain unavailable while the UAT route is Next-owned.
5. Run desktop and mobile Playwright projects and axe WCAG A/AA scans.
6. Recount and fingerprint all five student-directory relations after the browser tests.

## Route rollback

With one authenticated UAT session returning HTTP 200, set only the two UAT student-directory
routes to `LEGACY`/`LEGACY_WRITE` and require HTTP 404. Restore them to
`NEXT`/`NEXT_READ_ONLY` and require HTTP 200 without restarting AIS Next. Reconfirm that the local
routes were not changed.

## Recovery rehearsal

Temporarily point only the UAT CORE descriptor to an unused localhost port. Keep the original URL
in memory and restore it from a `finally` path. After the configured idle pool expires, require the
API to return HTTP 503 with Problem Details type
`urn:ais-next:problem:student-directory-unavailable` and no JDBC URL, port, credential, or driver
detail. Restore the exact clone URL and require HTTP 200 on the same application PID.

Never stop PostgreSQL or repoint the UAT descriptor at a source database to simulate recovery.

## Cleanup

Delete temporary menu assignments by their exact role/menu keys and restore the UAT user's exact
original role tuple. Require zero temporary grants, unchanged source role state, matching
source/clone schema and projection fingerprints, the intended final route decisions, application
health `UP`, and the same running PID. Retain only sanitized screenshots and evidence metadata.
