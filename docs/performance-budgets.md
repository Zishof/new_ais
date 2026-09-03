# Initial performance budgets

These are starting gates, not measured production promises. Capture a baseline on the target infrastructure and tighten or amend via an ADR.

| Signal | Initial budget |
|---|---:|
| Cold startup to readiness | <= 20 s |
| Idle process heap after warm-up | <= 384 MiB |
| Cached tenant pools | <= configured maximum, default 32 |
| Connections per tenant/database pool | <= 4 default, <= 20 hard control-plane constraint |
| Pools opened for descriptor-only 1,000-tenant test | 0 |
| Role list SQL statements per request | <= 2 |
| Role list JSON payload at default page | <= 100 KiB |
| Read API p95 / p99 on local network | <= 500 ms / 1,000 ms |
| Initial HTML p95 | <= 750 ms |
| Error rate excluding client 4xx | < 0.5% over 15 min |

Record startup, heap, pool counts, active/idle connections, p50/p95/p99, SQL count, payload size, first contentful response and error rate per release candidate. Do not label a run passing if the load shape, tenant count or hardware is missing from the report.

Phase 4 local UAT remeasured two clean starts at 10.686 and 10.779 seconds. The final process used
156,357 KiB of a 278,528 KiB committed G1 heap. Both pass the initial local startup and idle-heap
budgets; they are not a substitute for target-environment release-candidate measurements.

Phase 5 local UAT started the current application artifact in 9.436 seconds. After browser,
rollback, and two controlled recovery checks, PID 31940 used 177,764 KiB of a 278,528 KiB committed
G1 heap. This passes the initial local startup and idle-heap budgets but is not target-environment
load evidence.

When active tenant concurrency approaches PostgreSQL limits, put PgBouncer in transaction-pooling mode after validating prepared statements, session variables and read-only initialization. Continue bounding application pools; PgBouncer is not permission to multiply them.
