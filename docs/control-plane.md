# Control-plane model

`ais_next_control` is the only database migrated by AIS Next. It must have independent backup, least-privilege credentials and no foreign keys to tenant databases.

```mermaid
erDiagram
  TENANT ||--o{ TENANT_DOMAIN : owns
  TENANT ||--o{ TENANT_DATABASE : routes
  TENANT ||--o{ TENANT_SECRET_REFERENCE : references
  TENANT ||--o{ TENANT_MODULE_ROUTE : gates
  TENANT ||--o{ TENANT_FEATURE_FLAG : enables
  TENANT ||--o{ TENANT_API_CLIENT : authenticates
  TENANT ||--o{ TENANT_SCHEMA_FINGERPRINT : records
  TENANT ||--o{ TENANT_MIGRATION_STATE : owns
  TENANT ||--o{ AUDIT_EVENT : records
  TENANT ||--o{ OUTBOX_EVENT : emits
  TENANT ||--o{ FILE_SAGA : coordinates
```

`tenant_database` has exactly one descriptor per `(tenant, CORE|FILE)`. It stores a JDBC URL and credential reference, never the credential. `tenant_module_route` determines which application handles a route while `tenant_migration_state` tracks aggregate write ownership; these are related but deliberately separate controls. `security_handoff_nonce` atomically prevents token replay and stores only a SHA-256 nonce digest.

Flyway migration `db/control/V1__control_plane.sql` creates these tables. It is wired to the explicitly named control datasource, not either routing datasource.
