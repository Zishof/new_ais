# Legacy security findings

Captured 3 September 2026 from the operator-provided local tree. This is a risk-oriented structural review, not a penetration test.

| ID | Finding | Evidence | Risk | Required treatment |
|---|---|---|---|---|
| SEC-01 | A plaintext/no-op password encoding path is present in legacy configuration. | One source/config hit for the plaintext encoder pattern. | Critical if reachable. | Verify the active authentication chain; retain legacy verification only as a quarantined adapter, then rehash on successful login into a modern credential store. |
| SEC-02 | The application exposes 182 servlet mappings across ZK, Axis, Jersey, JSP and callbacks. | `WEB-INF/web.xml`. | Large and heterogeneous attack surface. | Inventory consumers and authentication per mapping before any route change. |
| SEC-03 | Login coexistence cannot safely share serialized HTTP sessions across framework generations. | Spring Security 3.x/Servlet 2.5 baseline versus Spring Security 7/Tomcat 11 target. | Session fixation, deserialization and privilege-context errors. | Use a short-lived signed token with issuer, audience, tenant, active role, nonce and one-time consumption. Rotate the Next session ID. |
| SEC-04 | Tenant selection supplied by a browser parameter would permit cross-tenant confused-deputy attacks. | Multi-database target architecture. | Critical data isolation failure. | Resolve tenant only from an allowlisted host and bind it to the authenticated token/session. Negative leakage tests are mandatory. |
| SEC-05 | Public file/repository surfaces require a dedicated authorization audit. | Repository, journal-file, galley, PDF and document servlet mappings. | Possible unauthorized disclosure. | Keep routes legacy-owned until object-level authorization and range/content headers have contract tests. |
| SEC-06 | Payment and external callbacks are mixed with browser servlet endpoints. | Payment/journal callback mappings and integration sources. | Forgery/replay/idempotency failures. | Require per-consumer signature, replay window, idempotency key and safe audit before migration. |
| SEC-07 | Credentials must not be copied from tenant metadata into source or logs. | New multi-tenant registry requirement. | Secret disclosure. | Control plane stores only secret references; runtime values come from environment or an approved secret provider. |

The localhost test used operator-supplied credentials only as process environment values. They are not committed. Production must use separate least-privilege accounts; legacy descriptors are configured read-only and execute `set default_transaction_read_only = on`.
