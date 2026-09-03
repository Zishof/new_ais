# Phase 2 local UAT and performance evidence — 2026-09-04

## Scope and environment

- Candidate: Phase 2 identity read-only slice and tenant route gate on branch
  `codex/ais-next-foundation`.
- Host: Microsoft Windows 11 Pro 10.0.22621, Intel Core i5-6500 (4 cores / 4 logical
  processors), 63.9 GiB RAM.
- Runtime: Eclipse Temurin OpenJDK 25.0.4.1; PostgreSQL 16.4 on `localhost:5432`.
- Load shape: Playwright Chromium with two projects (desktop and Pixel 7 emulation). Each project
  performed five warm-up API calls, 40 measured role-list API calls, and 15 measured role-list HTML
  navigations. The two projects ran through the default two-worker test configuration.
- The tenant CORE database was queried through a read-only descriptor. The control database was the
  only database changed for nonce consumption, tenant bootstrap, and the route rollback rehearsal.

## Functional, security, and accessibility result

- Maven reactor: passed, including route-policy/filter tests, identity authorization/search tests,
  JavaDoc coverage, and Spring Modulith verification.
- Docker-backed migration test: skipped because Docker is not installed on this host; the same
  Flyway V1 migration was validated and found current against local `ais_next_control` at startup.
- Browser UAT: 18/18 tests passed across desktop and mobile projects.
- Covered landing, one-time handoff, dashboard redirect, role list, profile, global search, versioned
  APIs, menu privilege denial, unassigned-role denial, token replay denial, responsive rendering,
  and automated WCAG A/AA checks.
- Legacy RBAC table counts before and after UAT were unchanged: `tbmrole=57`,
  `job_has_menu=3489`, `menu=1305`, and `role_privilage=4486`.

## Route rollback rehearsal

The local `/roles` decision was changed from `NEXT` to `LEGACY` in `ais_next_control` while
`write_ownership` remained `NEXT_READ_ONLY`. The AIS Next endpoint returned HTTP 404. After an
application restart the row remained `LEGACY` at version 1, proving bootstrap did not overwrite the
operator rollback, and the endpoint still returned HTTP 404. The route was then restored to `NEXT`
at version 2 and the authenticated role-directory smoke test passed. No tenant database row was
changed.

## Measured budgets

| Signal | Budget | Desktop | Mobile emulation | Result |
|---|---:|---:|---:|---|
| Read API p50 | report | 11 ms | 8 ms | recorded |
| Read API p95 | <= 500 ms | 16 ms | 13 ms | pass |
| Read API p99 | <= 1,000 ms | 38 ms | 23 ms | pass |
| Initial HTML p95 | <= 750 ms | 490 ms | 669 ms | pass |
| Role-list JSON payload | <= 100 KiB | 1,140 bytes | 1,140 bytes | pass |
| Role-list SQL statements | <= 2 | 2 | 2 | pass |
| Idle heap after warm-up | <= 384 MiB | 82.9 MiB used / 240 MiB committed | n/a | pass |
| Tenant/database pools | CORE <= 4, FILE lazy | CORE 1, FILE 0; control 2 | n/a | pass |
| Cold startup to readiness | <= 20 s | 22.634 s first run; 19.090 s repeat | n/a | needs follow-up |

The startup budget is not marked passing because one of two measured cold process starts exceeded
20 seconds. The second run passed after filesystem and class data were warm. This is a local-host
variance finding, not a production promise; collect a larger startup sample on the target UAT
infrastructure and optimize or amend the budget through an ADR before release sign-off.
