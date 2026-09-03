# Phase 3 clone UAT runbook

This runbook creates isolated PostgreSQL databases for write testing. It must never target `ais`,
`streaming_ais`, or any other source/production database with restore, migration, or mutation
commands.

## Safety preflight

1. Confirm the exact source names and that the intended clone names do not exist.
2. Record source database sizes, aggregate row counts, and table fingerprints in read-only mode.
3. Store dumps under a task-specific directory below `C:\opt\NEW_AIS\.scratch`; verify its
   resolved absolute path before removal.
4. Supply PostgreSQL credentials through the process environment. Never commit passwords.

Approved local clone names for this iteration are:

- core: `ais_next_uat_clone_20260904`
- file: `streaming_ais_next_uat_clone_20260904`

Use PostgreSQL 16 `pg_dump`, `createdb`, and `pg_restore`. Create a custom-format dump, create an
empty clone, restore with `--exit-on-error`, and compare schema and data fingerprints. If restore
fails, retain the dump for diagnosis and drop only the incomplete clone after its exact name has
been independently verified.

## Control-plane registration

Register the clone as a distinct `uat-local` tenant and domain `uat.localhost`. Its CORE descriptor
starts read-only. Add route and aggregate state as `LEGACY`/`LEGACY_WRITE`. Only after all audit
preconditions pass may an operator atomically set the clone's organization route to `NEXT`, the
aggregate to `NEXT_WRITE`, and the CORE descriptor to writable. The normal `local` tenant remains
read-only and must never inherit these values.

## Rollback

1. Reject new commands and drain in-flight requests.
2. Set the UAT organization route owner to `LEGACY` while leaving evidence intact.
3. Confirm AIS Next returns 404 for the governed route.
4. Compare the last business row, Envers revision, and control audit event.
5. Restart AIS Next and confirm the `LEGACY` route decision persists.
6. Restore `NEXT` only after the discrepancy is resolved. Do not enable legacy and Next writes at
   the same time.

The clone databases are disposable only after evidence has been retained and their exact names are
confirmed. Removing a clone is destructive and requires an explicit cleanup decision; the source
databases are never cleanup targets.
