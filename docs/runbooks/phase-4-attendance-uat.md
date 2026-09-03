# Phase 4 daily attendance UAT runbook

This runbook validates the read-only attendance projection. All non-empty fixtures belong only in
an isolated clone. Never insert employees, attendance rows, test users, or roles into `ais` or
`streaming_ais`.

## Preflight

1. Require source CORE/FILE descriptors to be read-only and record source row, sequence, and status
   fingerprints with PostgreSQL `default_transaction_read_only=on`.
2. Confirm the UAT domain resolves to a distinct tenant whose CORE descriptor names the clone.
3. Confirm normal `local` and UAT attendance routes both begin `LEGACY`/`LEGACY_WRITE`.
4. Supply database credentials and the handoff key only through process environment.
5. Start AIS Next with localhost bootstrap disabled for a pre-registered non-local UAT tenant.

## Non-empty contract fixture

Use explicit negative primary keys so the two legacy sequences cannot advance. Insert three active
employees in deterministic name order. Give the first employee two rows on one date with different
negative IDs, the second one row, and the third no row. Use existing attendance statuses. Record
the exact IDs so cleanup never relies on a broad name match or recursive operation.

Temporarily assign the UAT user one active role that owns menu 10000269 READ and retain an active
assigned role without that authority for the negative test. Record all original role columns before
the update.

Set only the two UAT attendance prefixes to `NEXT`/`NEXT_READ_ONLY`. Do not change aggregate write
ownership and do not make the normal local route Next-owned.

## Required checks

1. Handoff the positive role and reach `/dashboard`, then `/attendance/daily`.
2. Require the HTML and API to show three/all, two/recorded, and one/unrecorded.
3. Require the higher attendance ID to win for the duplicate employee/date pair.
4. Verify name/number filter, page boundaries, invalid enum, oversized page, and SQL-shaped text.
5. Handoff the active negative role and require HTTP 403 from both HTML/API attendance routes.
6. Run desktop and mobile Playwright projects with axe WCAG A/AA tags.
7. Recount all fixture tables and assert no GET created, updated, or removed a business row.

## Route rollback

With an authenticated session returning HTTP 200, change both UAT attendance prefixes to
`LEGACY`/`LEGACY_WRITE` and require HTTP 404. Restore them to `NEXT`/`NEXT_READ_ONLY` and require
HTTP 200 without restarting. Recheck that the normal local tenant stayed 404.

## Recovery rehearsal

Use an unused local TCP port in the UAT CORE descriptor rather than stopping PostgreSQL. Use a short
pool idle TTL only for this rehearsal so the cached healthy pool is evicted deterministically.
Require the API to return a safe HTTP 503 problem without connection details. Restore the exact
clone JDBC URL, allow the failed pool to expire, and require HTTP 200 on the same application PID.

The descriptor restoration belongs in a `finally` path. Never leave a dead URL in the control
plane, and never repoint the UAT descriptor at a source database as a recovery shortcut.

## Cleanup

Delete attendance fixtures before employee fixtures using the exact recorded IDs. Restore every
changed user role column. Require zero fixture rows, unchanged sequences, identical source/clone
status fingerprints, healthy application status, and the intended final route state. Retain only
sanitized screenshots and the evidence report.
