# Phase 3 school-type clone UAT — 2026-09-04

## Outcome

The technical UAT candidate passed on an isolated PostgreSQL clone pair. AIS Next reached the
dashboard through the one-time handoff and rendered the `Jenis sekolah` home/list page on desktop
and mobile. The source `ais` and `streaming_ais` databases were not modified.

This evidence does not approve production rollout. The business UAT signature, live legacy ZK
cache/UI reverse-visibility test, independent control-audit/outbox design, and cold-start budget
remain open gates.

## Exact environment and artifact

- Java: OpenJDK 25.0.4.1
- PostgreSQL: 16.4 at `localhost:5432`
- Application: Spring Boot 4.1.1 on `http://localhost:8081`
- UAT host/tenant: `http://uat.localhost:8081`, `uat-local`
- CORE clone: `ais_next_uat_clone_20260904`
- FILE clone: `streaming_ais_next_uat_clone_20260904`
- JAR SHA-256: `428DF4189E690441F0809DE944DFE5A268166A4FE2882569F6BD16FABA3F77CF`
- Running evidence process after rollback restart: PID 12172

The database password and handoff signing key were supplied only through process environment and
are not stored in the repository.

## Clone proof and isolation

`pg_dump -Fc` plus PostgreSQL 16 `pg_restore --exit-on-error --no-owner` produced the clone pair.
Before UAT, source and clone each had 3,526 base tables, 56,997 columns, six school-type rows, 22
referencing school rows, 4,862 Envers revisions, and `hibernate_sequence=5121969`. Their complete
school-type physical fingerprint was `d64aa8ebc9c05ca6a37ad1d5188502b1`.

After UAT, source `ais` still has six rows, no `jenis_sekolah__audit` rows,
`hibernate_sequence=5121969`, and semantic fingerprint
`c8905dc8a6842ccd6c5c53491aa336d9`. The clone also has six business rows and the same semantic
fingerprint. UAT-only audit timestamps and actors intentionally make its full physical fingerprint
different.

Final routing state:

| Tenant | CORE | Organization route | Aggregate ownership |
|---|---|---|---|
| `local` | `ais`, read-only | `LEGACY` / HTTP 404 | no Next write state |
| `uat-local` | clone, writable | `NEXT`, version 3 | `NEXT_WRITE`, version 1 |

The clone-only authorization fixture activates role `amp` and assigns it to
`admin.user_role2`. Direct verification confirms the source still has inactive `amp` and a blank
`admin.user_role2`.

Temporary custom-format dumps could not be removed because the host blocked the destructive file
operation. They remain at `C:\opt\NEW_AIS\.scratch\phase3-clone-20260904`. The verified Excel
artifact remains at `C:\opt\NEW_AIS\.scratch\phase3-jenis-sekolah-uat.xlsx` with SHA-256
`2A12A96277FF328D7DCFA6F6F7E1D289C7A52C32B1FF62743177E8571709B78A`.

## Functional and security evidence

Manual authenticated API rehearsal returned:

- create: HTTP 201;
- update and active toggle: HTTP 200;
- stale update using the original ETag: HTTP 409;
- delete of ID 1, referenced by three schools: HTTP 409;
- delete/cleanup of the UAT-created unreferenced row: HTTP 204.

The create/update/delete sequence produced exactly one Envers row of each revision type (0, 1,
2), used consecutive shared revisions 5121970–5121972, and left no business fixture row. The final
clone contains 24 audit snapshots: six creates, twelve updates, and six deletes. This includes
manual UAT, the six-row atomic Excel round trip, and cleaned-up desktop/mobile Playwright fixtures.
The source audit table remains empty.

Excel export returned HTTP 200, the OOXML content type, and a 4,028-byte workbook. Importing that
export returned `{"created":0,"updated":6}` with an audit delta of exactly six and no row-count
change. Unit tests also prove fixed headers, round-trip values/tokens, row bounds, and rejection of
formula cells.

Server-side authorization derived from menu 881247 was checked with positive role `amp`; role
`Dosen` received HTTP 403. Tenant `local` continued returning HTTP 404 even with an authenticated
UAT session.

## Automated tests

- Maven reactor `test`: passed, including all Java class/method JavaDoc checks and Spring Modulith
  architecture verification. The Docker-backed clean control-migration test was skipped because
  Docker is unavailable; Flyway validation against the real local control database passed at app
  startup.
- Playwright desktop suite: all 12 scenarios passed.
- Playwright mobile serial rerun: all 12 scenarios passed.
- Focused Phase 3 rerun: 8/8 passed (four scenarios on each desktop/mobile project).
- WCAG tags scanned: WCAG 2 A/AA, 2.1 AA, and 2.2 AA; no automatically detectable violation on
  the school-type catalogue.

The initial parallel mobile run passed 22/24 scenarios but two already-successful identity tests
exceeded the 30-second browser-context teardown limit under host contention. The configured timeout
was raised to 60 seconds and the complete mobile suite passed serially. This was a harness flake,
not an endpoint assertion failure.

Measured local-network read evidence after warm-up:

| Project | API p50 | API p95 | API p99 | HTML p95 | Role payload |
|---|---:|---:|---:|---:|---:|
| Desktop Chromium | 9 ms | 12 ms | 14 ms | 302 ms | 1,139 bytes |
| Mobile Chromium | 7 ms | 9 ms | 23 ms | 274 ms | 1,139 bytes |

Heap after UAT was approximately 164.6 MiB used / 240 MiB committed, below the 384 MiB budget.
AIS Next held two idle control connections and one idle UAT CORE connection, each below configured
pool limits. Connections visible on source `ais` and `streaming_ais` belonged to pgAdmin, not AIS
Next.

## Rollback rehearsal

Both UAT organization routes were changed from `NEXT` to `LEGACY` while write ownership remained
`NEXT_WRITE`; the endpoint returned HTTP 404. AIS Next was restarted and the route remained
`LEGACY`/404, proving bootstrap does not undo the operator decision. Routes were then restored to
`NEXT` at version 3 and a fresh handoff plus school-type page returned HTTP 200.

The restart took 27.3 seconds and therefore failed the current cold-start budget of 20 seconds.
The earlier start completed in 14.283 seconds. The variance must be profiled and re-measured on the
target UAT host before Phase 3 can exit.

## Open production gates

1. Obtain product-owner UAT acceptance for labels, validation, and Excel workflow.
2. Start a compatible legacy ZK instance against the clone, refresh/restart its preloaded cache,
   and prove Next-to-ZK and ZK-to-Next visibility through the actual UI.
3. Approve a transactional outbox/reconciliation design for the independent AIS Next control audit;
   PostgreSQL CORE and control databases are intentionally not joined with XA.
4. Diagnose the 27.3-second cold-start outlier and meet or amend the 20-second budget via ADR.
5. Retain route and aggregate ownership controls; never point the writable descriptor at `ais`.
