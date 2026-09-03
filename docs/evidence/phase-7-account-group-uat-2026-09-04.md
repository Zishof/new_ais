# Phase 7 account-group UAT evidence — 2026-09-04

## Result

The first Phase 7 Finance slice passed technical UAT on the isolated
`ais_next_uat_clone_20260904` CORE clone. A fresh one-time handoff reached the dashboard and the
responsive `/finance/account-groups` page. The API returned all seven global reference rows
through a stable paged projection containing only ID, name, and description. No write route exists
in this slice.

Production promotion remains gated by finance-owner acceptance, independent security and
reconciliation review, operations readiness, and target-environment performance evidence.

## Build and runtime identity

- Branch: `codex/ais-next-foundation`
- Application artifact SHA-256:
  `B58C95C9AED98C4F2C48807F6C8E3A12125DDEE8C2EE5E10F56C1E33FCD37BEC`
- Java: local JDK 25 toolchain
- Application PID: `22096`
- Startup to readiness: `9.277 s`
- Final health: `UP`
- Final G1 heap: `278,528 KiB` committed, `172,542 KiB` used
- Source CORE/FILE descriptors: read-only
- UAT CORE descriptor: isolated clone; restored after recovery rehearsal

The database password, handoff signing key, and one-time tokens were supplied only at runtime and
are absent from the repository and this evidence.

## Automated checks

The full 25-project Maven reactor passed, including all five new Finance unit tests,
repository-wide JavaDoc coverage, dependency policy, and Spring Modulith boundaries. The
Testcontainers-only control-plane test was skipped because Docker was unavailable.

The final Playwright run passed six test executions: three scenarios on Desktop Chrome and the
same three on Pixel 7. Together they proved:

- fresh handoff to dashboard and account-group page;
- total seven and exact three-field response minimization;
- deterministic paging and case-insensitive literal name filtering;
- literal wildcard and escape-character handling;
- page-size 101 returning HTTP 400;
- an active assigned role without menu 36332 returning HTTP 403 from HTML and JSON;
- the local legacy-owned page returning HTTP 404; and
- zero axe violations for WCAG 2.0 A/AA, 2.1 AA, and 2.2 AA tags.

An initial browser run passed four executions and found that the expected warning sentence was
narrower than the actual safety warning. The test was aligned with the rendered contract; no
application behavior changed, and the complete rerun passed 6/6.

Both final screenshots were visually inspected. Navigation, warning, filter, two-column table,
and count remain readable on desktop and mobile.

| Screenshot | SHA-256 |
|---|---|
| Desktop account-group directory | `DE725803A70E7FEF003B9C5ECB15A4FFF69CEBB9A62CD79F47F9986D981380CF` |
| Mobile account-group directory | `814BED2EA60105436FEE9979204EF6BC2300EB58C7FF56A34B90AE79F2AB8D1F` |

Sanitized copies are retained outside the repository under
`C:\opt\NEW_AIS\.scratch\phase7-account-group-uat-20260904`.

## Authorization and fixture cleanup

Clone role `amp` was already active and had effective menu 36332 read authority, so the positive
test required no menu or privilege fixture. The UAT user's original role tuple was
`amp / amp / null`. Only `user_role2` was temporarily changed to the existing active `Dosen` role
for the no-menu negative; it was restored to `amp` in an exact cleanup operation.

Final source checks found no `aisnext_uat` user and retained the original inactive source `amp`
role. Final clone checks found `aisnext_uat` restored to `amp / amp / null` and retained the
pre-existing active clone `amp` role. No role, menu, privilege, or account-group row was created or
deleted.

## Route rollback

Using one authenticated `amp` session against PID `22096`:

1. the UAT JSON route returned HTTP 200 as `NEXT`/`NEXT_READ_ONLY`;
2. switching only the two UAT account-group routes to `LEGACY`/`LEGACY_WRITE` produced HTTP 404;
3. restoring `NEXT`/`NEXT_READ_ONLY` produced HTTP 200; and
4. the process remained alive throughout.

The final route state is:

| Tenant | Route | Owner | Write ownership |
|---|---|---|---|
| `local` | `/finance/account-groups` | `LEGACY` | `LEGACY_WRITE` |
| `local` | `/api/v1/finance/account-groups` | `LEGACY` | `LEGACY_WRITE` |
| `uat-local` | `/finance/account-groups` | `NEXT` | `NEXT_READ_ONLY` |
| `uat-local` | `/api/v1/finance/account-groups` | `NEXT` | `NEXT_READ_ONLY` |

## Database outage and recovery

After successful authentication, only the UAT CORE descriptor was temporarily changed to an
unused localhost port. The API returned HTTP 503 with exact Problem Details type
`urn:ais-next:problem:account-groups-unavailable`. Its decoded detail contained no dead port, JDBC
URL, password, or connection diagnostics.

The exact clone URL was restored from a `finally` path. The same session returned HTTP 200 after
pool expiry, health was `UP`, and PID `22096` remained alive.

## Source invariance

Before and after UAT, source and clone matched the audited baseline exactly:

- schema fingerprint: `29c7870af4432e79b47da260d410c07b`;
- minimized projection fingerprint: `7cbbb5cceddc4bf0e3a697f9fdf474e6`; and
- eligible rows: `7`.

No query or test changed `akunting.grup_akun`. After role cleanup, a fresh `amp` handoff returned
HTTP 200 for the dashboard and account-group page, and the API again reported seven rows on PID
`22096`.

## Remaining gates

1. Obtain finance-domain owner acceptance for the three-field projection and global-data
   semantics.
2. Complete independent security and reconciliation review despite the absence of monetary data.
3. Confirm operations readiness, ownership, escalation contacts, and rollback authority.
4. Measure latency and resource budgets in the target environment under representative load.
5. Keep accounts, balances, transactions, journals, posting, closing, tax, payments, approvals,
   audit, and reconciliation legacy-owned until each receives its own audited contract.
