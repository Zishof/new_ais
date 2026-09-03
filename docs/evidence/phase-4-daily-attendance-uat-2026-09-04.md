# Phase 4 daily attendance clone UAT — 2026-09-04

## Outcome

The read-only daily employee-attendance vertical slice passed its technical UAT on the isolated
CORE clone. A one-time legacy handoff reached the dashboard and the daily monitor on desktop and
mobile. API paging/filtering, deterministic duplicate selection, exact menu authorization, route
rollback, source isolation, and database outage/recovery all passed.

This evidence does not authorize production routing. The attendance domain owner must still accept
the projection and its explicit exclusion of render-time repair behavior. Product-owner acceptance
and the independent control-audit/outbox design also remain shared production gates.

## Environment and artifact

- Git commit containing the UAT suite and accessibility correction: `98ea29a`.
- Java: OpenJDK 25.0.4.1.
- PostgreSQL: 16.4 at `localhost:5432`.
- Spring Boot: 4.1.1 on `http://localhost:8081`.
- UAT tenant/host: `uat-local`, `http://uat.localhost:8081`.
- CORE clone: `ais_next_uat_clone_20260904`.
- FILE clone: `streaming_ais_next_uat_clone_20260904`.
- JAR SHA-256: `B3B181533ECD0B0D6428BF0BC4500F05F654795099C63C003AF0DB398A379B92`.
- Final AIS Next process: PID 29344.
- Isolated legacy process retained from Phase 3: PID 25328 on port 18080.

Passwords, the handoff signing key, and one-time tokens were supplied only at runtime and are not
stored in the repository.

## Source isolation and fixtures

Discovery and final source checks used `default_transaction_read_only=on`. Source `ais` contained
zero employees, zero daily attendance rows, and no `aisnext_uat` account. The clone also started
with zero employees and zero daily attendance rows.

The non-empty rehearsal inserted only three clone employees with negative IDs and three clone
attendance rows: Alpha had two same-day rows, Beta had one, and Gamma had none. Negative explicit
IDs kept `pegawai_id_seq=350` and `status_kehadiran_karyawan_harian_id_seq=10038` unchanged. The
positive user temporarily received role `am`; its original `amp` assignment was restored.

After every test, exactly three attendance rows and three employee rows were deleted. Final clone
counts are again zero/zero, none of the six negative IDs remains, and the UAT user's role columns
match their pre-test values. Source and clone attendance-status fingerprints both equal
`0ada97a9283caa5f1bae3bf4e9f65ddd`. The only source connections observed were the existing pgAdmin
sessions; AIS Next and the isolated legacy runtime stayed on the clone pair.

## Functional and authorization evidence

Authenticated requests using role `am`, which owns legacy menu 10000269 READ, returned:

| Check | Result |
|---|---:|
| Handoff to dashboard | HTTP 200 after redirect |
| All employees | 3 |
| `RECORDED` | 2 |
| `UNRECORDED` | 1 |
| Duplicate winner for Alpha | attendance ID `-904101`, note `latest duplicate` |
| Filter `UAT-002` | exactly Beta |
| Page 1 at size 1 | exactly Beta |
| SQL-shaped search text | zero matches, no query expansion |
| Server-rendered monitor | HTTP 200 |
| Invalid record state | HTTP 400 |
| Page size 101 | HTTP 400 |

Role `amp` is active and assigned to the UAT user but does not own menu 10000269. Its handoff
succeeded and the attendance API returned HTTP 403. This proves the endpoint is protected by the
exact legacy menu authority, independently of navigation visibility. The inactive legacy role
`maha` was not used as the negative because it is rejected earlier at handoff, which would not test
endpoint authorization.

No selected request changed employee, attendance, sequence, or status data. The Next projection
did not call the legacy `loadData`, synchronization, or `autoUpdatePulangDariSejarah` paths.

## Browser and accessibility evidence

The focused Playwright suite ran three scenarios on both Desktop Chrome and Pixel 7 emulation.
The first pass exposed one serious axe finding: the horizontally scrollable table was not keyboard
focusable. Adding a labeled region and `tabindex=0` fixed it. The final run passed 6/6, including
WCAG 2 A/AA, 2.1 AA, and 2.2 AA automated checks with zero violations.

Rendered evidence is retained outside the repository:

- `C:\opt\NEW_AIS\.scratch\phase4-attendance-uat-20260904\daily-attendance-desktop.png`;
- `C:\opt\NEW_AIS\.scratch\phase4-attendance-uat-20260904\daily-attendance-mobile.png`.

The Maven reactor also passed with the attendance module's seven unit tests, repository-wide
JavaDoc coverage, and Spring Modulith verification. The Docker-backed clean control-plane migration
test was the only skipped test because Docker is unavailable; Flyway validation passed against the
real local control database at every application start.

## Route rollback and database recovery

Normal tenant `local` retained `LEGACY`/`LEGACY_WRITE` for both attendance prefixes and returned
HTTP 404 throughout. UAT began in the same closed state, then changed only its two attendance
prefixes to `NEXT`/`NEXT_READ_ONLY`.

The rollback rehearsal changed both UAT prefixes to `LEGACY`; an already authenticated API request
changed from HTTP 200 to 404. Restoring `NEXT_READ_ONLY` returned HTTP 200 without restarting AIS
Next. Final UAT route version is 3 for both prefixes; local remains version 0 and legacy-owned.

For recovery, the UAT CORE descriptor was temporarily pointed at an unused local port after a
successful request. With the UAT-only pool idle TTL set to one second, the next request returned
HTTP 503 and `application/problem+json` type
`urn:ais-next:problem:attendance-unavailable`, without exposing the JDBC exception. The descriptor
was restored, the failed pool aged out, and the same authenticated session returned HTTP 200 on the
same PID. Health remained HTTP 200. No PostgreSQL server or source database was stopped.

## Resource and startup evidence

Two clean starts with non-local bootstrap disabled completed in 10.686 and 10.779 seconds, below the
20-second cold-start budget and materially below the earlier 27.3-second outlier. The final G1 heap
reported 156,357 KiB used and 278,528 KiB committed, below the 384 MiB idle-heap budget. The final
control plane held two idle connections. Combined Next and legacy clone connections remained below
the configured database limits.

The earlier outlier is retained in Phase 3 history, but repeated local remeasurement now clears the
technical cold-start budget for this artifact. Target-environment measurement is still required for
a production release candidate.

## Remaining gate

The Phase 4 technical candidate is complete. Production promotion remains blocked until the
attendance domain owner confirms that the latest-ID duplicate rule, active/null-active employee
scope, empty source state, and omission of legacy repair writes match the intended business view.
