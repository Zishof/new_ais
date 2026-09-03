# ADR-0005: Select persistence per use case and enforce one writer

- Status: Accepted
- Date: 2026-09-03
- Owners: AIS architecture and data teams

## Context

The legacy database contains thousands of tables and awkward report queries. A single ORM persistence unit mapping everything would increase startup, memory and accidental-write risk. Coexistence also makes uncoordinated dual writes unsafe.

## Decision

Use Hibernate/JPA only for bounded transactional aggregates with stable relationships. Use `JdbcClient` projections for dashboards, reports, high-volume reads, PostgreSQL-specific queries and awkward legacy joins. Never serialize entities directly to JSON.

Every migrated aggregate follows exactly one state: `LEGACY_WRITE`, `NEXT_READ_ONLY`, `NEXT_SHADOW`, `NEXT_WRITE`, `LEGACY_READ_ONLY`, or `RETIRED`. Only one application is the primary writer. The first role/menu slice is `NEXT_READ_ONLY`; no CRUD endpoint exists.

Cross-database file operations use a saga plus outbox/retry/reconciliation, never XA or best-effort dual writes.

## Alternatives considered

- ORM for all tables: rejected due to mapping size and unsuitable report shapes.
- JDBC for all writes: rejected because well-bounded aggregates benefit from ORM concurrency and unit-of-work semantics.
- Active-active dual writers: rejected because it makes ordering, auditing and rollback ambiguous.

## Consequences

Teams must make and test persistence decisions per aggregate. Moving to `NEXT_WRITE` requires clone-based mapping, concurrency, audit equivalence, reverse visibility and rollback proof. A file saga can be temporarily inconsistent by design and requires operational reconciliation.
