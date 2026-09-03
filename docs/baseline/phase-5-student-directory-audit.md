# Phase 5 contract audit: Direktori Siswa Sekolah

`sekolah.siswa` is the first Phase 5 academic-core candidate. The selected increment is a
read-only, server-paged student directory. It deliberately does not migrate student create,
update, delete, password, photo, RFID, class assignment, report-card, or bulk-transfer behavior.
The machine-readable companion is
[`phase-5-student-directory-contract.json`](phase-5-student-directory-contract.json).

Discovery used PostgreSQL read-only sessions against `ais` and the isolated
`ais_next_uat_clone_20260904` clone. The two databases had the same schema and projection
fingerprints at audit time. No source row was changed.

## Legacy boundary

Legacy menu `887727`, **Siswa**, opens
`/pages/master/sekolah/siswa.zul` and applies
`ais.action.master.sekolah.SiswaAction`. The audited runtime artifacts have these SHA-256 hashes:

- ZUL: `E92C731BFD46FF678C7AFCC826E0C93A8022BD1C89907940440C1A71B7CB4BD1`
- action class: `23F7D802B6B13DC7878DE5B86FB0CEC50A38B6E35BFC711A1C47487FEF643EE9`

The legacy page mixes a paged directory with broad write and export capabilities. Its default
criteria require a nonblank name and a school, treat `aktif is null` as active, order by entry
year descending then student number ascending, and expose many filters. The first Next slice
preserves only the safe directory subset: active students, deterministic ordering, a bounded
name-or-number search, and server-side school/foundation scope from the active legacy role.

## Data minimization

The response contains only:

- legacy student ID;
- student number;
- display name;
- entry year;
- school and current-class display names;
- initial and exit-status display names; and
- normalized active state.

It excludes passwords and tokens, national identifiers, birth data, addresses, telephone and
email fields, parent or guardian data, financial history, health data, religion, social-media
profiles, biometrics, photos, signatures, and free-form serialized fields. These exclusions are
part of the contract, not merely a UI choice.

## Query and role semantics

The Next projection uses parameterized SQL only. It joins the display columns from
`sekolah.sekolah`, `sekolah.kelas`, `sekolah.status_awal_siswa`, and
`sekolah.status_keluar_siswa`; it never hydrates the 174-column legacy entity.

The authenticated `HandoffPrincipal.activeRoleId` is bound to `public.tbmrole`. A result is
visible only when the role is active and both of these predicates hold:

```text
role.yayasan is null or siswa.yayasan_id = role.yayasan
role.sekolah is null or siswa.sekolah_id = role.sekolah
```

This prevents an authenticated user from widening school scope through request parameters. The
text filter is matched case-insensitively against student number or name. Page size is limited to
100 rows. Default ordering is `tahun_masuk desc`, `nomor_induk asc`, `id asc`.

## Authorization and route ownership

Read requires `LEGACY_MENU_887727_READ`, derived server-side from the active legacy role. The
audited privilege table contains an active `am` grant and an inactive `amp` grant; role activity
is still checked during handoff. The source `am` role is foundation-scoped and currently sees no
student rows, which is the expected secure result rather than a reason to bypass role scope.

The browser route is `/academic/students` and the versioned JSON route is
`/api/v1/academic/students`. Both default to `LEGACY`/`LEGACY_WRITE`. Only the isolated UAT tenant
may be promoted to `NEXT`/`NEXT_READ_ONLY`; there is no Next write route in this increment.

## Baseline on 2026-09-04

| Metric | Value |
|---|---:|
| `sekolah.siswa` rows | 3 |
| `sekolah.kelas` rows | 84 |
| `sekolah.sekolah` rows | 22 |
| `sekolah.status_awal_siswa` rows | 7 |
| `sekolah.status_keluar_siswa` rows | 10 |
| eligible global projection rows | 3 |
| five-table schema fingerprint | `4bc1104b1923ac957634bbf1bc7b620c` |
| minimized projection fingerprint | `a32508c409c760dea6520f8672edc3b4` |

The source and UAT clone matched all three fingerprint/count values. Names and student numbers
were included only inside the one-way projection fingerprint; the audit evidence does not copy
those personal values.

## Explicitly excluded side effects

The following legacy behavior remains owned by ZK and must not be invoked by a directory GET:

- student create/update/delete and password encryption;
- photo, signature, RFID, password, and bulk file upload/download;
- current class, tutoring class, dormitory, counselor, or homeroom changes;
- candidate-student conversion and initial/exit-status workflow;
- payment, deposit, library, activity, report-card, and transcript operations; and
- any lazy getter that opens a Hibernate session or computes related financial data.

The Next implementation must contain SELECT statements only and a read-only transaction.

## Phase 5 slice gates

- Unit tests prove page bounds, principal/role propagation, and normalized filter behavior.
- Repository review proves exact selected columns, role scoping, parameter binding, and stable
  ordering.
- Authorization tests prove an authenticated role without menu `887727` receives 403.
- Clone UAT proves source/clone baseline parity, non-empty role-scoped data, browser login and
  directory rendering, API paging/filtering, desktop/mobile accessibility, route rollback, and
  source invariance.
- Database outage/recovery must return a safe 503 and recover in the same process.
- Production promotion remains blocked on student-domain owner acceptance and privacy review.

