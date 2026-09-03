# Risk register

| ID | Risk | Likelihood | Impact | Mitigation/owner | Trigger |
|---|---|---:|---:|---|---|
| R1 | Hidden legacy business rule is missed | High | Critical | Per-aggregate deep audit, shadow/read comparison, domain owner | Any parity mismatch |
| R2 | Cross-tenant leakage | Medium | Critical | Host allowlist, token binding, context cleanup, two-clone negatives | Tenant mismatch or unexplained query |
| R3 | Legacy schema changes outside Next | High | High | Fingerprint monitoring and compatibility gate | Fingerprint drift |
| R4 | Connection explosion | Medium | High | Lazy bounded pools, eviction, small limits, PgBouncer | 70% DB connection capacity |
| R5 | Two applications write one aggregate | Medium | Critical | Control-plane ownership state and route gate | Ownership/traffic disagreement |
| R6 | Auth token leaks via logs | Medium | High | One-time/short TTL, redaction, POST exchange plan | Token appears in telemetry |
| R7 | Unknown API consumer breaks | High | High | Consumer registry, versioned facade, telemetry/deprecation window | Unidentified production traffic |
| R8 | Audit semantics diverge | Medium | High | Legacy/Next audit ports and equivalence test | Missing actor/before-after/request ID |
| R9 | Cross-database file orphan | Medium | High | Saga/outbox/retry/reconciliation; no XA | Saga age exceeds SLO |
| R10 | Commercial dependency enters build | Low | High | Enforcer denylist, allowlist review and SBOM | Unknown/prohibited license |
| R11 | Control-plane outage blocks handoff/routing | Medium | High | Backup, health checks, cached-safe routing policy, legacy rollback | Control DB unavailable |
| R12 | Big-bang expectations overrun scope | Medium | High | Gated roadmap and explicit phase reporting | Cutover proposed without evidence |
| R13 | A legacy attendance read triggers hidden repair writes | High | Critical | SELECT-only Next projection; keep synchronization/editing legacy-owned until separately audited | Any attendance GET changes a row or sequence |
| R14 | A broad student entity exposes sensitive personal data | High | Critical | Explicit projection allowlist, response-shape tests, privacy review, sanitized evidence retention | Any unapproved student field appears in API, logs, or screenshots |
| R15 | A library reference cutover accidentally pulls catalog publication or circulation writes into Next | Medium | Critical | Separate item-type read contract and routes; keep all catalog/circulation/acquisition/file behavior legacy-owned | A read-only route invokes or exposes a catalog mutation |
| R16 | A finance reference cutover is mistaken for authority to migrate monetary workflows | Medium | Critical | Separate account-group SELECT-only contract and routes; require independent finance, security, and reconciliation gates for every later aggregate | A reference GET changes data or a transaction/journal/payment route is proposed without its own contract |
| R17 | A Legacy route is retired before hidden consumers or rollback obligations expire | Medium | Critical | Per-route zero-traffic evidence, consumer disposition, named approvals, retained artifacts, restore proof, and an explicit rollback window | Any retirement request lacks dated production evidence or bundles multiple first-time routes |
