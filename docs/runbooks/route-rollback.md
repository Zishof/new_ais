# Route and write-ownership rollback

For the current read-only slice, rollback is immediate: set the reverse-proxy route owner to legacy and stop AIS Next. No tenant database rollback exists or is required because AIS Next performs no write there.

For a future write slice:

1. Freeze the affected route and reject new commands.
2. Drain in-flight requests and outbox/file sagas; reconcile ambiguous outcomes.
3. Capture audit and data comparison evidence.
4. Move aggregate state through the approved transition to `LEGACY_READ_ONLY`/`LEGACY_WRITE`; never enable two writers.
5. Route traffic to legacy, run a known read/write smoke case on a clone or approved production-safe record, and monitor.
6. Retain the Next artifact and control metadata for the rollback window; do not drop tables.

Rollback triggers include tenant leakage, audit mismatch, unexplained data divergence, error budget breach, unresolved file saga, or consumer contract regression.
