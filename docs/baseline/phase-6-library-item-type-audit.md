# Phase 6 contract audit: Jenis Item Perpustakaan

`library.jenis_item` is the first Supporting ERP candidate. The selected increment is a read-only,
server-paged directory of library item types. It deliberately excludes the much broader
`library.item` catalog because that legacy page combines catalog maintenance, bulk publication,
files, availability, classification, and many cross-module relationships. The machine-readable
companion is
[`phase-6-library-item-type-contract.json`](phase-6-library-item-type-contract.json).

Discovery used PostgreSQL read-only sessions against `ais` and the isolated
`ais_next_uat_clone_20260904` clone. Source and clone had identical schema and projection
fingerprints. No row was changed.

## Legacy boundary

Legacy menu `56141`, **Jenis Item**, opens `/pages/master/library/jenis_item.zul` and applies
`ais.action.master.library.JenisItemAction`. The audited runtime artifacts have these SHA-256
hashes:

- ZUL: `9F4018581A11A50D5F3EB4E36C7BB9E0F0F1BDBD66B140DE3E79B2EFA7755DF2`
- action class: `0A5876433E42D8ED12E704BB7F946546C4B02703AC5A16A92E6C241FD620BF6D`

The legacy list filters `nama` with case-insensitive `ANYWHERE` matching, orders by name ascending,
and pages using the common legacy page size. The same composer also exposes create, update,
duplicate-name validation, and delete listeners. Those write paths are outside this contract.

## Data contract

The response contains only:

- legacy item-type ID;
- trimmed item-type name; and
- normalized description, with null represented as an empty string.

It excludes `oleh`, `olehid`, `tanggal_dirubah`, and the internal `defaultitem` flag. The Next query
must enumerate the three approved values and must not hydrate the legacy entity.

All 31 source rows have nonblank unique names and nonblank descriptions. The longest observed name
and description are both 22 characters. The table has no organization, foundation, school, or
library ownership column, so this reference data is global after authorization; request parameters
cannot introduce a tenant-scope predicate that the legacy relation does not contain.

## Query semantics

The Next projection uses parameterized SQL only. It requires a nonblank name, applies a literal,
case-insensitive name-or-description filter, and orders by `nama asc, id asc` for a stable tie-break.
Percent, underscore, and backslash in user input remain literals. Page size is limited to 100.

The 31-row table has no name index. A bounded scan is acceptable for this audited size, but schema
and row-count drift must trigger plan review rather than an unapproved source index change.

## Authorization and route ownership

Read requires `LEGACY_MENU_56141_READ`, derived only when the active role has both a
`job_has_menu` assignment and `_read=1` in `role_privilage`. Source roles `admPerpus` and `am` are
active and have effective CRUD bits; `amp` has the same rows but is inactive in source. The Next
slice honors only the read authority even when the legacy role has write bits.

The browser route is `/supporting/library/item-types` and the versioned JSON route is
`/api/v1/supporting/library/item-types`. Both default to `LEGACY`/`LEGACY_WRITE`. Only the isolated
UAT tenant may be promoted to `NEXT`/`NEXT_READ_ONLY`; there is no Next write route in this
increment.

## Baseline on 2026-09-04

| Metric | Value |
|---|---:|
| `library.jenis_item` rows | 31 |
| eligible projection rows | 31 |
| null or blank names | 0 |
| duplicate normalized names | 0 |
| null or blank descriptions | 0 |
| schema fingerprint | `62062fd460e1f78aee71928b54bd3fc4` |
| minimized projection fingerprint | `2ca4bda70481dfdc40dd9282ac3f8150` |

Source and UAT clone matched both fingerprints and the projection count. The evidence stores only
one-way fingerprints, not business values.

## Explicitly excluded side effects

The following behavior remains legacy-owned and must not be invoked by a directory GET:

- create, update, and delete of item types;
- duplicate-name validation used by a write flow;
- mutation of existing `library.item` references;
- catalog publication or withdrawal;
- barcode, inventory, circulation, acquisition, file, and cover-image operations; and
- DDC/UDC, author, publisher, category, topic, member, or library-location changes.

The Next implementation must contain SELECT statements only and a read-only transaction.

## Phase 6 slice gates

- Unit tests prove bounds, normalization, literal wildcard escaping, and service delegation.
- Repository tests prove approved columns, parameter binding, and deterministic ordering.
- Authorization tests prove an active role without effective menu 56141 receives HTTP 403.
- Clone UAT proves source/clone parity, browser login and rendering, API paging/filtering,
  desktop/mobile accessibility, route rollback, and source invariance.
- Database outage/recovery must return a safe 503 and recover in the same process.
- Production promotion remains blocked on library-owner acceptance, a consumer/operations review,
  and target-environment performance evidence.
