# Tenant routing proof

## Trust chain

```text
HTTP Host (normalized, port removed)
  -> TenantCatalog exact domain lookup
  -> ResolvedTenant in request attribute and TenantContext
  -> handoff token tenant equality check
  -> TenantDataSourceKey(TenantId, CORE|FILE)
  -> descriptor from control-plane
  -> secret-reference resolution
  -> lazy, bounded Hikari pool
```

There is no controller/request parameter named `tenant`, `database` or `schema` that affects routing. An unknown host returns 404. Assets and the liveness probe avoid tenant database access.

## Isolation controls

- `TenantContext.open` rejects nested replacement and always clears through `AutoCloseable` scope.
- The data source key combines tenant and database role, preventing CORE/FILE aliasing.
- `TrustedHostTenantResolver` performs exact normalized-host lookup.
- Handoff refuses a signed token whose tenant differs from the host tenant.
- Legacy descriptors are `read_only=true`; pool creation sets JDBC read-only and PostgreSQL `default_transaction_read_only=on`.
- Pool cache is lazy and bounded; 1,000 descriptors are tested without opening 2,000 pools.

## Evidence and remaining proof

Unit tests cover context cleanup/replacement and lazy pool creation. The localhost smoke run validates actual host-to-tenant-to-`tbmrole` routing. Before production, add two tenant clones and a negative test proving a session/token for A cannot read B, plus proxy tests for spoofed `Forwarded`/`X-Forwarded-Host`. `forward-headers-strategy` remains `none` until the trusted proxy topology is configured.
