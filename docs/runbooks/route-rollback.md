# Route and write-ownership rollback

For the current read-only slice, rollback is immediate and tenant-specific. The application checks
`tenant_module_route` after resolving the trusted tenant host. A governed route is served only when
its most-specific row has `route_owner = 'NEXT'`; `LEGACY` returns HTTP 404 and a missing decision
fails closed with HTTP 503. Local bootstrap inserts missing Phase 2 rows but never overwrites an
existing operator decision.

## Read-only identity rollback

Run these statements against `ais_next_control`, never against a legacy tenant database. Replace
the tenant key after verifying it with the first query.

```sql
select t.tenant_key, r.module_key, r.route_pattern, r.route_owner,
       r.write_ownership, r.version
from tenant_module_route r
join tenant t on t.id = r.tenant_id
where t.tenant_key = 'local' and r.module_key = 'identity'
order by r.route_pattern;

begin;
update tenant_module_route r
set route_owner = 'LEGACY', version = version + 1
where r.tenant_id = (select id from tenant where tenant_key = 'local')
  and r.module_key = 'identity'
  and r.route_owner = 'NEXT';
commit;
```

Verify that a governed AIS Next URL returns HTTP 404, move the corresponding reverse-proxy routes
to the legacy application, and monitor errors. AIS Next can remain running for unaffected tenants
and modules. No tenant database rollback exists or is required because AIS Next performs no write
there.

To restore the proven read-only slice after approval:

```sql
begin;
update tenant_module_route r
set route_owner = 'NEXT', version = version + 1
where r.tenant_id = (select id from tenant where tenant_key = 'local')
  and r.module_key = 'identity'
  and r.route_owner = 'LEGACY'
  and r.write_ownership = 'NEXT_READ_ONLY';
commit;
```

Verify the authenticated list/detail smoke test after restoration. Never change
`write_ownership` as part of a route-only rollback.

For a future write slice:

1. Freeze the affected route and reject new commands.
2. Drain in-flight requests and outbox/file sagas; reconcile ambiguous outcomes.
3. Capture audit and data comparison evidence.
4. Move aggregate state through the approved transition to `LEGACY_READ_ONLY`/`LEGACY_WRITE`; never enable two writers.
5. Route traffic to legacy, run a known read/write smoke case on a clone or approved production-safe record, and monitor.
6. Retain the Next artifact and control metadata for the rollback window; do not drop tables.

Rollback triggers include tenant leakage, audit mismatch, unexplained data divergence, error budget breach, unresolved file saga, or consumer contract regression.
