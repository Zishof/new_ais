# Phase 7 contract audit: Grup Akun Keuangan

`akunting.grup_akun` is the first Finance candidate. The selected increment is a read-only,
server-paged account-group reference directory. It does not migrate accounts, balances,
transactions, journals, posting, tax, cash, payments, approvals, or reconciliation. The
machine-readable companion is
[`phase-7-account-group-contract.json`](phase-7-account-group-contract.json).

Discovery used PostgreSQL read-only sessions against `ais` and the isolated
`ais_next_uat_clone_20260904` clone. Source and clone had identical schema and projection
fingerprints. No financial row was changed.

## Legacy boundary

Legacy menu `36332`, **Setup Grup Akun**, opens `/pages/master/akunting/grupakun.zul` and applies
`ais.action.master.akunting.GrupAkunAction`. The audited runtime artifacts have these SHA-256
hashes:

- ZUL: `005BB318EA4EF5DD4065D42B664F8AADA6F69DAD5BCEAB188F3DEE58430FD2AA`
- action class: `86B107614C193773B233F36CA4178C42475C1373300B30158C27319156CA1A36`

The legacy list filters `nama` with case-insensitive matching, displays name and description, and
uses the shared grid/paging behavior. The same composer exposes create, update, duplicate-name
validation, and delete listeners. Every write path remains outside this contract.

## Data contract

The response contains only:

- legacy account-group ID;
- trimmed group name; and
- normalized description, with null represented as an empty string.

It excludes `oleh`, `olehid`, and `tanggal_dirubah`. The Next query enumerates the approved values
and never hydrates the legacy entity.

All seven source rows have nonblank unique names. Two descriptions are null or blank and normalize
to an empty string. The longest observed name is 24 characters and the longest description is 50
characters. There is no organization, foundation, or school ownership column, so the reference is
global after authorization.

## Query semantics

The projection uses parameterized SQL only. It requires a nonblank name, applies a literal,
case-insensitive name filter, and orders by `nama asc, id asc` for deterministic paging. Percent,
underscore, and backslash in user input remain literal. Page size is limited to 100.

The seven-row relation has no name index. A bounded scan is acceptable at the audited size; schema
or cardinality drift triggers plan review rather than an unapproved source index change.

## Authorization and route ownership

Read requires `LEGACY_MENU_36332_READ`, derived only when the active role has both a
`job_has_menu` assignment and `_read=1` in `role_privilage`. Source roles `am` and `keu` are active
with effective CRUD bits; `amp` has equivalent menu/privilege rows but is inactive in source. The
Next slice honors only read authority even if the role has legacy write bits.

The browser route is `/finance/account-groups` and the versioned JSON route is
`/api/v1/finance/account-groups`. Both default to `LEGACY`/`LEGACY_WRITE`. Only the isolated UAT
tenant may be promoted to `NEXT`/`NEXT_READ_ONLY`; the slice defines no Next write route.

## Baseline on 2026-09-04

| Metric | Value |
|---|---:|
| `akunting.grup_akun` rows | 7 |
| eligible projection rows | 7 |
| null or blank names | 0 |
| duplicate normalized names | 0 |
| null or blank descriptions | 2 |
| schema fingerprint | `29c7870af4432e79b47da260d410c07b` |
| minimized projection fingerprint | `7cbbb5cceddc4bf0e3a697f9fdf474e6` |

Source and UAT clone matched both fingerprints and the projection count. Evidence retains only
one-way fingerprints, not financial reference values.

## Explicitly excluded side effects

The following behavior remains legacy-owned and must not be invoked by a directory GET:

- create, update, or delete of account groups;
- cascading changes to `akunting.akun`;
- duplicate-name validation used by a write flow;
- account, balance, transaction, journal, posting, closing, tax, cash, reimbursement, or payment
  operations;
- approval, rejection, transfer, or standing-instruction workflows; and
- audit or reconciliation writes.

The Next implementation must contain SELECT statements only and a read-only transaction.

## Phase 7 slice gates

- Unit tests prove page bounds, normalization, literal wildcard escaping, and service delegation.
- Repository tests prove exact columns, parameter binding, and deterministic ordering.
- Authorization tests prove an active assigned role without effective menu 36332 receives 403.
- Clone UAT proves source/clone parity, browser login and rendering, API paging/filtering,
  desktop/mobile accessibility, route rollback, and source invariance.
- Database outage/recovery must return a safe 503 and recover in the same process.
- Production promotion remains blocked on finance-owner acceptance, independent security and
  reconciliation review, operations readiness, and target-environment performance evidence.
