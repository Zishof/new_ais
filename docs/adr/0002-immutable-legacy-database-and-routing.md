# ADR-0002: Treat tenant databases as immutable schemas and route lazily

- Status: Accepted
- Date: 2026-09-03
- Owners: AIS architecture and DBA teams

## Context

AIS legacy and AIS Next must coexist over the same tenant data. Tenants may use separate CORE and FILE databases. Eagerly opening two large pools for every tenant exhausts PostgreSQL connections, while modifying legacy schemas can break the existing Hibernate mappings and code.

## Decision drivers

- Zero schema regression in existing tenant databases.
- Strict tenant isolation.
- Bounded connection consumption at hundreds or thousands of descriptors.
- Explicit write ownership with reversible cutover.

## Decision

Tenant database schemas are immutable contracts: no `CREATE`, `ALTER`, `DROP`, triggers, rename, sequence changes, or automatic Hibernate DDL. `spring.jpa.hibernate.ddl-auto=none` and OSIV is disabled. New mappings use Jakarta/JdbcClient projections with exact legacy names and types.

A separate Flyway-owned `ais_next_control` database stores tenant/domain/database descriptors, secret references, route and write ownership, schema fingerprints, migration state, nonces, audit, outbox and file-saga state.

Tenant resolution starts from a normalized allowlisted host and is later checked against authentication. Browser query parameters cannot select a tenant or database. `TenantDataSourceRegistry` creates Hikari pools on first use, keeps a bounded LRU-like cache, evicts idle pools, closes evicted pools, and resolves credentials by reference. Legacy read descriptors set both JDBC read-only and PostgreSQL `default_transaction_read_only`.

## Alternatives considered

- One global pool or schema switch: rejected because AIS uses separate physical databases and a leaked schema/search path is unsafe.
- One eager pool per tenant/database: rejected due to connection explosion.
- Modify legacy schemas to fit new entities: rejected because coexistence would be broken.

## Consequences

Database contract discovery becomes a release gate. The control-plane is a new operational dependency and requires backup. A cache miss adds connection setup latency. PgBouncer is recommended when measured active-tenant concurrency approaches PostgreSQL connection limits.

## Rollback

Set the route owner back to `LEGACY`, keep write ownership at `LEGACY_WRITE`, and stop AIS Next. No legacy schema rollback is needed because it is never migrated by Next.
