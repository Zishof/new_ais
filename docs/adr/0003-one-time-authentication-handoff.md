# ADR-0003: Bridge authentication with a one-time signed handoff

- Status: Accepted
- Date: 2026-09-03
- Owners: AIS security team

## Context

The two applications use incompatible servlet and Spring Security generations. Sharing a serialized session would couple internal classes, weaken isolation and risk deserialization/session-fixation flaws. Requiring a second login damages adoption, while retaining plaintext credentials is unacceptable.

## Decision drivers

- Preserve the legacy-authenticated user and active role.
- Bind authentication to the resolved tenant.
- Prevent replay, tampering and session fixation.
- Avoid password or serialized-session exchange.

## Decision

Legacy issues a short-lived HMAC-SHA-256 handoff containing version, issuer, audience, tenant, user, active role, high-entropy nonce and expiration. AIS Next validates the signature in constant-time, exact issuer/audience, expiration and tenant/host binding, then atomically consumes a hashed nonce in the control-plane. It creates a fresh Spring Security context and rotates the session ID.

The signing key is at least 256 bits and enters only through a secret provider/environment variable. Tokens are never persisted in clear text. Production should prefer POST or a one-time exchange code so gateway access logs do not retain a query token.

## Alternatives considered

- Shared HTTP session: rejected due to framework serialization coupling and fixation risk.
- Shared plaintext/password hash: rejected because hashes are not bearer credentials and old encoding may be weak.
- Immediate external IdP cutover: strategically desirable but not a prerequisite for the first coexistence slice.

## Consequences

Clock synchronization, key rotation and nonce cleanup are required. A control-plane outage prevents new handoffs but does not alter legacy sessions. Role parity must be tested server-side; a token role is context, not permission proof for arbitrary operations.

## Rollback

Remove the AIS Next route and continue the existing legacy session. Revoke/rotate the handoff key and clear only expired nonce records.
