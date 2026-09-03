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

This iteration implements Phase 0 deliverables, Phase 1 foundation, the complete Phase 2 read-only
identity slice, and a technical Phase 3 UAT candidate for `sekolah.jenis_sekolah`. It also completes
the technical Phase 4 read-only daily employee-attendance candidate and starts Phase 5 with an
audited, data-minimized school-student directory contract. Candidate routes are enabled only for
the isolated `uat-local` clone; the normal `local` tenant remains legacy-owned.

Phase 3 is not production-complete. The isolated live ZK compatibility rehearsal now proves both
Next-to-ZK and ZK-to-Next UI visibility with API cleanup and source-database isolation. Product-owner
acceptance and an independent control-audit/outbox design remain exit gates. Two subsequent clean
AIS Next starts completed in 10.686 and 10.779 seconds, clearing the local 20-second technical
budget; target-environment measurement is still required. Legacy deployment sizing must retain the
measured 4 GiB heap floor and roughly 130-second cold-start allowance for this dataset.

Phase 4 technical UAT is complete, including the documented same-process database outage/recovery
and route rollback rehearsals. Production promotion additionally requires the attendance domain
owner's parity acceptance. The source currently has no employee or daily-attendance rows, so its
empty-state parity passed while non-empty validation remained clone-only and was fully cleaned up.

The first Phase 5 contract selects `/academic/students` as a read-only, role-scoped projection of
`sekolah.siswa`. It excludes credentials, contact/parent/health/financial data and every write or
bulk-file operation from the broad legacy page. Implementation and clone UAT remain required
before this slice becomes a technical candidate; production additionally requires academic-owner
acceptance and privacy review.
