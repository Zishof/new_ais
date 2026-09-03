# Phase 8 route-retirement runbook

Phase 8 removes one verified legacy compatibility path at a time. A technically successful UAT is
not retirement authority. Execute this runbook only for an exact tenant, route, and aggregate with
production evidence and named approvers.

## Per-route entry record

Before changing traffic, record all of the following in an immutable change record:

1. tenant, module, aggregate, exact browser/API prefixes, current route owner, and write ownership;
2. deployed AIS Next artifact and database/schema compatibility fingerprints;
3. domain-owner acceptance and security/privacy approval;
4. reconciliation approval for any state that has ever accepted writes;
5. a dated production observation window showing zero calls to the Legacy route, including known
   service accounts, jobs, exports, bookmarks, and external consumers;
6. consumer-registry disposition, deprecation communications, and escalation contacts;
7. successful Next smoke, authorization-negative, rollback, and outage-recovery evidence;
8. retention/archive obligations, backup restore proof, and the approved rollback-window length;
9. gateway, deployment, database, and operations owners who will execute or observe the change;
   and
10. objective abort thresholds for errors, latency, data divergence, audit gaps, and support load.

Any blank, stale, or contradictory item closes the gate.

## Retirement sequence

1. Freeze unrelated route changes and capture current gateway and control-plane state.
2. Reconfirm the zero-traffic window immediately before the change.
3. Remove only the exact Legacy gateway mapping while retaining the Legacy artifact and its data.
4. Prove the Next browser/API route, authentication, authorization negatives, and observability.
5. Monitor the agreed error-budget window. Restore the exact gateway mapping on any abort trigger.
6. Retain deployable Legacy artifacts, backups, configuration, and operator knowledge for the
   approved rollback window.
7. After that window expires and retention owners approve, remove the compatibility deployment and
   archive its evidence. Do not drop shared legacy tables as part of route retirement.
8. Keep the control-plane route row as historical enforcement metadata. For a completed migration,
   `route_owner` remains `NEXT`; use `write_ownership=RETIRED` only after the Legacy compatibility
   path is actually gone and the aggregate's owner approves that terminal state.

Deleting a governed control-plane row is not retirement: it intentionally causes AIS Next to fail
closed with HTTP 503.

## Verification and rollback

Verification must include status, payload contract, role/tenant negatives, dependency health,
latency/error signals, audit/reconciliation checks where relevant, and absence of new Legacy
traffic. Rollback restores the captured gateway state and the prior control-plane state in one
change window, then repeats the smoke and invariance checks.

Never combine multiple modules, tenants, or aggregates in the first retirement change. Never
retire based only on local/UAT traffic, delete shared source data, or let a missed deadline silently
convert into approval.
