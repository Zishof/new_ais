# Phase 8 retirement-readiness evidence — 2026-09-04

## Decision

No Legacy route is eligible for retirement from this local/UAT exercise. The Phase 8 gate remains
closed by design. No gateway rule, Legacy deployment, control-plane route row, source table, or
archive was deleted, and no route was marked `RETIRED`.

This is a successful readiness assessment, not a failed technical test: the missing inputs are
production evidence and external approvals that cannot be inferred from localhost behavior.

## Current governed-route inventory

The application governs 16 exact route prefixes per tenant:

| Module | Prefixes | `local` state | `uat-local` state | Retirement status |
|---|---:|---|---|---|
| Identity | 6 | `NEXT` / `NEXT_READ_ONLY` | `NEXT` / `NEXT_READ_ONLY` | Gate closed |
| Organization | 2 | `LEGACY` / `LEGACY_WRITE` | `NEXT` / `NEXT_WRITE` | Not cut over locally |
| Attendance | 2 | `LEGACY` / `LEGACY_WRITE` | `NEXT` / `NEXT_READ_ONLY` | Not cut over locally |
| Academic core | 2 | `LEGACY` / `LEGACY_WRITE` | `NEXT` / `NEXT_READ_ONLY` | Not cut over locally |
| Supporting ERP | 2 | `LEGACY` / `LEGACY_WRITE` | `NEXT` / `NEXT_READ_ONLY` | Not cut over locally |
| Finance | 2 | `LEGACY` / `LEGACY_WRITE` | `NEXT` / `NEXT_READ_ONLY` | Not cut over locally |

The six local Identity prefixes are the only non-UAT routes currently Next-owned. They are not
eligible for retirement because this workspace provides neither production gateway traffic nor a
completed production rollback window. The other ten local prefixes are still explicitly
Legacy-owned and therefore cannot have their Legacy paths removed.

## Gate evaluation

| Required evidence | Status | Reason |
|---|---|---|
| Exact route and ownership inventory | Present | Control plane and governed prefix catalog agree |
| Technical build/UAT through Phase 7 | Present | Maven, browser, rollback, recovery, and invariance evidence retained |
| Production Legacy traffic equals zero | Missing | No production telemetry source is in scope |
| Known and unknown consumer disposition | Missing | Local requests cannot establish the production consumer set |
| Domain-owner retirement acceptance | Missing | Technical execution is not owner sign-off |
| Security/privacy/reconciliation acceptance | Missing | Independent reviewers have not approved retirement |
| Retention/archive and restore approval | Missing | No approved production retention record was supplied |
| Production rollback window expired | Missing | The production window has not begun |
| Gateway/deployment change authority | Missing | No production gateway or deployment target is in scope |

Because multiple mandatory entries are missing, performing retirement would violate the roadmap's
Phase 8 exit gate.

## Controls retained

- All 32 tenant-route decisions remain present: 16 for `local` and 16 for `uat-local`.
- Missing route metadata continues to fail closed rather than masquerade as retirement.
- Normal local Phase 3–7 routes remain Legacy-owned.
- UAT route promotions remain isolated to clone-backed `uat-local`.
- Source CORE/FILE descriptors remain read-only.
- Phase-specific rollback and recovery evidence remains available for later production planning.

The executable procedure for an authorized future retirement is
[`phase-8-route-retirement.md`](../runbooks/phase-8-route-retirement.md). Each production route must
receive its own dated evidence record; this readiness assessment must not be reused as approval.
