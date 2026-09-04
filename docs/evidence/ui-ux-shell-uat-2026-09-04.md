# AIS Next UI/UX shell UAT evidence — 2026-09-04

## Result

The refreshed AIS Next entry, authenticated dashboard, and Finance account-group directory passed
desktop and mobile technical UAT against the isolated `uat-local` clone. All twelve final browser
executions passed: six visual-shell contracts and six account-group functional regressions.

The visual outcome and intentional coexistence differences are recorded in the
[`UI/UX parity audit`](../baseline/ui-ux-parity-audit.md).

## Tested source and runtime

- Source branch: `codex/ais-next-foundation`
- Application source commit: `b4391e40ec241d895331971bb9c54d1f287e0da1`
- Application artifact SHA-256:
  `3E7C452BE1F94A6A13BBB100E3D7A639535299FA238B5705341609DF21176FB1`
- Java: local JDK 25 toolchain
- Final application PID: `23332`
- Startup to readiness: `12.448 s`
- Final health: `UP`
- Next URL: `http://localhost:8081/`
- UAT tenant URL: `http://uat.localhost:8081/`
- Legacy login destination: `http://localhost:18080/ais/login`

The database password, signing key, sessions, and one-time handoff tokens were supplied only to
local processes and were not written to the repository or this evidence.

## Automated checks

The 22-project application dependency reactor passed. This included the repository-wide JavaDoc
coverage rule, Spring Modulith boundary checks, controller presentation-model regression, and all
module tests. The Testcontainers-only migration test was skipped because Docker was unavailable;
the live control-plane and clone checks below ran directly on PostgreSQL 16 instead.

The final visual-shell Playwright run passed these three scenarios on Desktop Chrome and Pixel 7:

1. public entry content, trusted Legacy login destination, and WCAG scan;
2. signed handoff, dashboard context, role-scoped services, responsive navigation width, active
   page state, and WCAG scan; and
3. Finance page active state, readable table, mobile overflow containment, and WCAG scan.

The separate final account-group regression also passed three scenarios on both devices. It
verified seven minimized reference rows, deterministic paging, literal filtering, bounds
validation, exact role authority, local-route fail-closed behavior, and a zero-violation axe scan.

## Findings closed during UAT

The first browser run found three issues that were fixed before acceptance:

- Thymeleaf could not evaluate a record-only role accessor through the wrapped Spring Security
  principal, so the dashboard returned HTTP 500. The controller now extracts the trusted handoff
  identity and passes presentation-safe scalar values, covered by a regression test.
- One secondary text color and translucent mobile entry surfaces fell just below the WCAG contrast
  threshold. The final palette uses darker secondary text and opaque high-contrast entry panels.
- The framework's flex rule placed desktop navigation beside the top row and constrained the
  mobile menu. The AIS shell now explicitly uses a two-level desktop layout and full-width mobile
  navigation, with a browser width assertion.

## Visual inspection

The final six full-page screenshots were inspected for clipping, hierarchy, active state, mobile
stacking, and table usability. Sanitized copies are retained outside the repository at
`C:\opt\NEW_AIS\.scratch\ui-ux-refresh-20260904`.

| Screen | SHA-256 |
|---|---|
| Public entry, desktop | `C635F504E9E101BC47CAFFC06B2BAEB81EE85BA4A217CC14F8ADFE937A0FD1F4` |
| Public entry, mobile | `310D5DA91E4877737B5E409B9F5F8E26970DEC7504968EF805AFEE90C0A55980` |
| Dashboard, desktop | `D77C84B7CE0A728D80154D729E2C03ADC854B74434C98114F1DD9EE53FD0DBF1` |
| Dashboard, mobile | `995CC6D40099994144B2B0F5085E7D3FE8829A684E354BE9E9FD926A32270E00` |
| Account groups, desktop | `DD66E98D8286D19D150D1D516258BEA06F8DEA24A0ECD19CEDC19858E386D12C` |
| Account groups, mobile | `16822CDB31C702C335E0980FDC29D95F946BBD3A5B8C2581ECE9A90413BDACB3` |

## Database boundary and cleanup

All browser data requests used the control-plane descriptor for the isolated
`ais_next_uat_clone_20260904` CORE clone. The source `ais` database was queried only in a read-only
session and still contains zero `aisnext_uat` users.

The account-group negative authorization scenario temporarily changed only the clone user's
`user_role2` from `amp` to the existing `Dosen` role. A `finally` cleanup restored the exact final
tuple to `amp / amp / null`. No account-group, menu, privilege, journal, balance, or payment row was
inserted, updated, or deleted by the UI tests.

## Remaining acceptance gates

1. Obtain product-owner acceptance for institution branding, wording, and representative roles.
2. Complete the independent security and operations reviews required by the migration roadmap.
3. Measure target-environment latency and resource budgets under representative load.
4. Promote only the routes whose individual ownership and rollback gates have passed; all other
   workflows remain in AIS Legacy.
