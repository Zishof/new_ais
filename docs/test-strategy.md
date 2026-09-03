# Test strategy

## Test pyramid and gates

1. Unit tests: value objects, tenant context leakage, token signature/replay/expiry, saga transitions and pagination validation.
2. Module/architecture tests: Maven dependency boundaries plus Spring Modulith/ArchUnit rules; no domain-to-web dependency or module cycle.
3. Repository mapping tests: the control-plane Flyway contract runs on disposable PostgreSQL with Testcontainers (skipped only when Docker is unavailable); legacy mappings require database clones and must assert exact columns, types, null/default semantics and query plans.
4. API contract tests: `/api/v1` JSON/OpenAPI compatibility and negative authorization.
5. Coexistence tests: on clones only, prove Next-to-legacy and legacy-to-Next visibility, one writer, audit equivalence, concurrency and route rollback.
6. Browser/accessibility tests: Playwright at desktop/mobile viewport, keyboard navigation and axe scans.
7. Performance/recovery tests: budgets, bounded pools, database outage, retry/reconciliation and control-plane restore.

## Current evidence

- Tenant context cannot be replaced while scoped and is cleared after exceptions.
- A catalog with 1,000 tenant descriptors opens zero pools before first access.
- Handoff token rejects expiration/tampering and consumes a nonce once.
- File saga validates state transitions.
- The localhost smoke test queries role data through a read-only tenant datasource.
- The Phase 3 school-type slice runs only on an isolated CORE/FILE clone pair and leaves `local`
  route ownership on `LEGACY` with a read-only source descriptor.
- School-type tests cover exact privilege negatives, paging/filter/sort, responsive WCAG scans,
  create/update/delete, stale ETag rejection, referenced-row conflict, Excel bounds/formula
  rejection, Envers revision types, source fingerprint invariance, and restart-persistent route
  rollback.

## Required before first write slice

- Sanitized CORE and FILE clones, never live tenant databases.
- Complete table and legacy getter/side-effect mapping for the selected aggregate.
- Privilege-negative, optimistic concurrency, audit-equivalence and rollback tests.
- Product owner UAT signature and current artifact hash.

The technical clone UAT now satisfies all automated items above, including live ZK/cache
reverse-visibility evidence in both directions. Production promotion remains blocked on
product-owner sign-off, independent control-audit/outbox design, and the cold-start performance
outlier.

The Phase 4 attendance increment adds parameterized projection tests, source empty-state parity,
clone-only recorded/unrecorded fixtures, deterministic duplicate selection, exact menu-authority
negatives, responsive accessibility, explicit no-write verification, and a CORE database
outage/recovery rehearsal. Full attendance editing and render-time repair behavior remain legacy
owned and are outside this read-only contract.

The first Phase 5 increment adds a minimized school-student projection. Its mandatory tests cover
active-role foundation/school scoping, exact menu `887727` authorization, active-only semantics,
bounded name-or-number filtering, stable paging order, absence of excluded personal columns,
source/clone fingerprint parity, responsive accessibility, same-process outage recovery, and
route rollback. All student writes, credentials, files, class assignment, and financial behavior
remain legacy owned.
