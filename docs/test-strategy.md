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

## Required before first write slice

- Sanitized CORE and FILE clones, never live tenant databases.
- Complete table and legacy getter/side-effect mapping for the selected aggregate.
- Privilege-negative, optimistic concurrency, audit-equivalence and rollback tests.
- Product owner UAT signature and current artifact hash.
