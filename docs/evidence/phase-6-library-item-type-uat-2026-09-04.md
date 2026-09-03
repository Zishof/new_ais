# Phase 6 library item-type UAT evidence — 2026-09-04

## Result

The first Phase 6 Supporting ERP slice passed technical UAT on the isolated
`ais_next_uat_clone_20260904` CORE clone. A fresh one-time handoff reached the dashboard and the
responsive `/supporting/library/item-types` page. The API returned all 31 global reference rows
through a stable paged projection containing only ID, name, and description. No write route exists
in this slice.

Production promotion remains gated by library-owner acceptance, consumer and operations review,
and target-environment performance measurement.

## Build and runtime identity

- Branch: `codex/ais-next-foundation`
- Application artifact SHA-256:
  `C39791D19E5E837202DFAB66ADE8E4033995BFC8888A7B22A141B8FA443D7A30`
- Java: local JDK 25 toolchain
- Application PID: `32364`
- Startup to readiness: `18.390 s`
- Final health: `UP`
- Final G1 heap: `221,184 KiB` committed, `88,426 KiB` used
- Source CORE/FILE descriptors: read-only
- UAT CORE descriptor: isolated clone; restored after recovery rehearsal

The database password, handoff signing key, and one-time tokens were supplied only at runtime and
are absent from the repository and this evidence.

## Automated checks

The full 24-project Maven reactor passed, including all five new Supporting ERP unit tests,
repository-wide JavaDoc coverage, dependency policy, and Spring Modulith boundaries. The
Testcontainers-only control-plane test was skipped because Docker was unavailable.

The final Playwright run passed six test executions: three scenarios on Desktop Chrome and the same
three on Pixel 7. Together they proved:

- fresh handoff to dashboard and item-type page;
- total 31 and exact three-field response minimization;
- deterministic paging and case-insensitive name-or-description filtering;
- literal wildcard and escape-character handling;
- page-size 101 returning HTTP 400;
- an active assigned role without menu 56141 returning HTTP 403 from HTML and JSON;
- the local legacy-owned page returning HTTP 404; and
- zero axe violations for WCAG 2.0 A/AA, 2.1 AA, and 2.2 AA tags.

An initial browser run passed four executions and exposed an ambiguous test locator because the
first row's name and description are identical. The locator was scoped to the first table row; the
application behavior did not change, and the complete rerun passed 6/6.

Both final screenshots were visually inspected. The navigation, filter, two-column table, and
pagination remain readable on desktop and mobile.

| Screenshot | SHA-256 |
|---|---|
| Desktop item-type directory | `DC73F6B565AC915AF5CFBB87EA48ACC6B505D7834713EDDA8FC23897F9B897F7` |
| Mobile item-type directory | `8D529A4BF31DF157CBD65AC5DDD0EEFE7FA763ECDA95184D52CA024EB410E3CD` |

Sanitized copies are retained outside the repository under
`C:\opt\NEW_AIS\.scratch\phase6-library-item-type-uat-20260904`.

## Authorization and fixture cleanup

Clone role `amp` was already active and had effective menu 56141 read authority, so the positive
test required no menu or privilege fixture. The UAT user's original role tuple was
`amp / amp / null`. Only `user_role2` was temporarily changed to the existing active `Dosen` role
for the no-menu negative; it was restored to `amp` in one exact transaction.

Final source checks found no `aisnext_uat` user and retained the original inactive source `amp`
role. Final clone checks found `aisnext_uat` restored to `amp / amp / null` and retained the
pre-existing active clone `amp` role. No role, menu, privilege, or item-type row was created or
deleted.

## Route rollback

Using one authenticated `amp` session against PID `32364`:

1. the UAT JSON route returned HTTP 200 as `NEXT`/`NEXT_READ_ONLY`;
2. switching only the two UAT item-type routes to `LEGACY`/`LEGACY_WRITE` produced HTTP 404;
3. restoring `NEXT`/`NEXT_READ_ONLY` produced HTTP 200; and
4. the process remained alive throughout.

The final route state is:

| Tenant | Route | Owner | Write ownership |
|---|---|---|---|
| `local` | `/supporting/library/item-types` | `LEGACY` | `LEGACY_WRITE` |
| `local` | `/api/v1/supporting/library/item-types` | `LEGACY` | `LEGACY_WRITE` |
| `uat-local` | `/supporting/library/item-types` | `NEXT` | `NEXT_READ_ONLY` |
| `uat-local` | `/api/v1/supporting/library/item-types` | `NEXT` | `NEXT_READ_ONLY` |

## Database outage and recovery

After successful authentication, only the UAT CORE descriptor was temporarily changed to an
unused localhost port. The API returned HTTP 503 with exact Problem Details type
`urn:ais-next:problem:library-item-types-unavailable`. Its decoded detail contained no dead port,
JDBC URL, password, or connection diagnostics.

The exact clone URL was restored from a `finally` path. The same session returned HTTP 200 after
pool expiry, health was `UP`, and PID `32364` remained alive.

## Source invariance

Before and after UAT, source and clone matched the audited baseline exactly:

- schema fingerprint: `62062fd460e1f78aee71928b54bd3fc4`;
- minimized projection fingerprint: `2ca4bda70481dfdc40dd9282ac3f8150`; and
- eligible rows: `31`.

No query or test changed `library.jenis_item`. After role cleanup, a fresh `amp` handoff returned
HTTP 200 for the dashboard and item-type page and the API again reported 31 rows on PID `32364`.

## Remaining gates

1. Obtain library-domain owner acceptance for the three-field projection and global-data semantics.
2. Confirm consumer and operations readiness, including ownership and escalation contacts.
3. Measure latency and resource budgets in the target environment under representative load.
4. Keep catalog writes, publication, circulation, acquisition, inventory, and files legacy-owned
   until each receives its own audited contract and rollback proof.
