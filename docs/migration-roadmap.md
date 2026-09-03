# Migration roadmap and gates

| Phase | Outcome | Entry/exit gate |
|---|---|---|
| 0 — Baseline | Source, DB, API, module, security and risk catalogs | Local snapshot recorded; unknown consumers remain explicit |
| 1 — Foundation | Java 25 modular monolith, control-plane, routing, security, UI, API, observability | Build/test, schema immutability proof, localhost read smoke |
| 2 — Read-only | Handoff, menu/dashboard/profile/search and one list/detail | Tenant/role negatives, responsive/accessibility and route rollback |
| 3 — Master CRUD | One low-risk reference aggregate | Clone-only bidirectional visibility, audit, concurrency, one writer, UAT |
| 4 — Vertical business slice | Calendar, attendance or repository candidate | Domain-owner parity and recovery rehearsal |
| 5 — Academic core | Student/curriculum/assessment slices | Measured module-by-module cutover |
| 6 — Supporting ERP | HR, asset, library and quality | Consumer and operational readiness |
| 7 — Finance/payment | Accounting and payment only after maturity | Reconciliation, security and rollback sign-off |
| 8 — Retirement | Remove legacy routes one at a time | Traffic zero, retention/archive and rollback window expired |

This iteration implements Phase 0 deliverables, Phase 1 foundation, and the role/menu portion of Phase 2. It does not enable a write endpoint. The next recommended slice is an organization reference master chosen jointly with the business owner and tested only against clones.
