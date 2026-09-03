# Phase 5 school-student directory UAT evidence — 2026-09-04

## Result

The first Phase 5 academic-core slice passed its technical UAT on the isolated
`ais_next_uat_clone_20260904` CORE clone. A fresh signed handoff reached the dashboard and the
responsive `/academic/students` page. The API returned the role-scoped, active-only three-row
projection while exposing only the nine approved fields. No student write path exists in this
slice.

Production promotion remains gated by academic-domain owner acceptance, privacy review, and
target-environment performance measurement.

## Build and runtime identity

- Branch: `codex/ais-next-foundation`
- Application artifact SHA-256:
  `64863F21052F035E9A741B80B7395811DDA438F89238848ACEFE8FB6D21D245A`
- Java: local JDK 25 toolchain
- Application PID: `31940`
- Startup to readiness: `9.436 s`
- Final health: `UP`
- Final G1 heap: `278,528 KiB` committed, `177,764 KiB` used
- Source CORE/FILE descriptors: read-only
- UAT CORE descriptor: isolated clone; restored after recovery rehearsal

The database password, handoff signing key, and one-time tokens were supplied only at runtime and
are not included in the repository or this evidence.

## Automated checks

The full Maven reactor passed, including unit tests, JavaDoc coverage, module-boundary tests, and
Spring Modulith verification. The Testcontainers-only control-plane test was skipped because a
Docker runtime was unavailable. The academic-core module contributed seven passing tests.

Playwright passed all six Phase 5 test executions: three scenarios in each configured project.
Together they covered:

- Desktop Chrome;
- Pixel 7 mobile;
- fresh handoff to dashboard and student directory;
- exact minimized response shape and three-row total;
- active-only semantics, deterministic paging, and literal wildcard filtering;
- foundation-scoped role returning zero rows rather than widening scope;
- page-size validation returning HTTP 400;
- active role without menu authority returning HTTP 403;
- local legacy-owned route returning HTTP 404; and
- zero axe violations for WCAG 2.0 A/AA, 2.1 AA, and 2.2 AA tags.

The final screenshots were visually inspected. The mobile table remains usable through horizontal
scrolling rather than clipping fields.

| Screenshot | SHA-256 |
|---|---|
| Desktop student directory | `F8B6BCEFCB477602BD1DF854C803D99A4855E950F2192B139DAD5B88462334D1` |
| Mobile student directory | `A04790C6908EC20393EBC07B8CD414C98C41234141CF5330FD91A9E8D0F7325D` |

Sanitized copies are retained outside the repository under
`C:\opt\NEW_AIS\.scratch\phase5-student-uat-20260904`.

## Effective authorization finding

The legacy authorization model requires both a menu assignment in `job_has_menu` and read bits in
`role_privilage`. Role `am` had the read privilege row but no source menu assignment, so it
correctly received HTTP 403 before the UAT fixture was added. One exact clone-only
`job_has_menu('am', 887727)` row enabled the foundation-scope test. The role's foundation scope
did not match the three students, and the authorized response was therefore an expected zero-row
page.

The temporary row was deleted after testing. No source authorization row was added or changed.

## Route rollback

Using one authenticated `amp` session against PID `31940`:

1. both UAT routes returned HTTP 200 as `NEXT`/`NEXT_READ_ONLY`;
2. switching only those routes to `LEGACY`/`LEGACY_WRITE` produced HTTP 404;
3. restoring `NEXT`/`NEXT_READ_ONLY` produced HTTP 200; and
4. the process did not restart.

The final route state is:

| Tenant | Route | Owner | Write ownership |
|---|---|---|---|
| `local` | `/academic/students` | `LEGACY` | `LEGACY_WRITE` |
| `local` | `/api/v1/academic/students` | `LEGACY` | `LEGACY_WRITE` |
| `uat-local` | `/academic/students` | `NEXT` | `NEXT_READ_ONLY` |
| `uat-local` | `/api/v1/academic/students` | `NEXT` | `NEXT_READ_ONLY` |

## Database outage and recovery

After a successful handoff, only the UAT CORE descriptor was temporarily changed to an unused
localhost port. The student API returned HTTP 503 with exact problem type
`urn:ais-next:problem:student-directory-unavailable`. The decoded detail contained no dead port,
JDBC URL, password, or connection diagnostics.

The exact clone URL was restored from a `finally` path. After pool expiry, the same authenticated
session returned HTTP 200, health was `UP`, and PID `31940` remained alive.

## Source invariance and cleanup

Before cleanup, source and clone both reported table counts `3 / 84 / 22 / 7 / 10` for student,
class, school, initial-status, and exit-status respectively. A post-UAT independent canonical hash
also matched between source and clone:

- five-table schema hash: `73c6c63c2acefc8b6d9d5648d7601bfa` on both;
- minimized projection hash: `5add720c98103e764e8d437e99cdd82a` on both; and
- eligible projection rows: `3` on both.

These post-UAT hashes use a separately canonicalized JSON/schema representation; the audit
contract's original fingerprints also remained represented by the unchanged counts and relation
content.

Cleanup ran in one exact clone transaction:

- deleted exactly one temporary `job_has_menu('am', 887727)` row;
- restored `aisnext_uat` from `amp / Dosen / am` to its original `amp / amp / null` role tuple;
- left the retained Phase 3 clone role `amp` active; and
- did not insert, update, or delete any student-directory business row.

Final source checks found no `aisnext_uat` user, no `am`/887727 menu assignment, and the original
inactive `amp` role. Final clone checks found the restored user tuple and zero temporary menu rows.

## Remaining gates

1. Obtain academic-domain owner parity acceptance for the minimized projection and scope rules.
2. Complete an explicit privacy review of response fields, logs, screenshots, and retention.
3. Measure latency and resource budgets in the target environment under representative load.
4. Keep all student writes, files, class assignment, and financial behavior legacy-owned until
   each receives its own audited contract and rollback proof.
