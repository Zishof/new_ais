# Authentication handoff threat model

## Protected assets

Tenant identity, user identity, active role, AIS Next session, signing keys and legacy authorization data.

| Threat | Control | Residual action |
|---|---|---|
| Token tampering | HMAC-SHA-256; constant-time signature comparison | Rotate keys and add key IDs before multiple issuers. |
| Replay | Unique issuer + hashed nonce with atomic insert | Scheduled deletion of expired nonce rows. |
| Cross-tenant token use | Host-derived tenant must equal signed tenant | Two-clone negative integration test before production. |
| Expired token | Strict `expiresAt > now` validation | Enforce a maximum issue lifetime at the issuer/gateway. |
| Session fixation | Fresh session and `changeSessionId()` | Browser test cookie rotation at the trusted proxy. |
| Token disclosure in URL/log | Short lifetime, one use, no token persistence | Prefer POST/exchange code and redact `token` in proxy/access logs. |
| Weak/committed signing secret | Minimum 32 UTF-8 bytes; environment reference | Use a managed secret provider and documented rotation overlap. |
| Role escalation | Role is signed and converted to a constrained authority | For writes, load authoritative privilege and ownership server-side. |
| CSRF after handoff | Spring Security CSRF remains enabled | Add browser tests for every future state-changing route. |
| Open redirect | Redirect target is fixed `/dashboard` | Keep return targets allowlisted if introduced. |

The handoff is an authentication bridge, not full authorization parity. The first slice exposes read-only role/menu data; every future operation must evaluate the same server-side privilege semantics as legacy.
