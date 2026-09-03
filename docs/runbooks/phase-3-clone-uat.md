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

## Live legacy compatibility rehearsal

Use a complete copy of the compatible Tomcat 7 deployment under a task-specific scratch directory;
do not edit the installed legacy runtime or source tree. Give the copy unique shutdown, HTTP, and
HTTPS ports. Override JNDI plus every active main/streaming Hibernate fallback to the exact clone
names, and pass credentials through local process environment or an ignored scratch properties
file. Verify the resolved active URLs before startup.

Use Java 8 with at least a 4 GiB heap for the audited dataset. A 1 GiB heap is insufficient and can
spend several minutes rebuilding Hibernate factories before ending with `GC overhead limit
exceeded`. Wait for the background cache executor to report that all tasks are complete before
sending the first browser request.

For the bidirectional gate:

1. Record source fingerprints and source/clone connections before starting Tomcat.
2. Reach the public page, open the login form, and authenticate with a clone-only fixture.
3. Select the role that owns menu 881247 and launch `Jenis Sekolah` through the main menu, not by a
   raw URL, so legacy privilege context is populated.
4. Create a uniquely named row through AIS Next, reload the ZK page, and prove the exact value is
   visible.
5. Create a second uniquely named row through the ZK modal and prove the authenticated AIS Next API
   returns it.
6. Delete both unreferenced fixtures through AIS Next with current ETags and require HTTP 204.
7. Recheck that source and clone business fingerprints match and that no application JDBC session
   appeared on either source database.

Legacy startup can run self-healing DDL/data routines. This is permitted only on disposable clones
and is another reason never to direct a legacy rehearsal at `ais` or `streaming_ais`.
