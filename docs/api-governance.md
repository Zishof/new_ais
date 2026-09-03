# API compatibility and governance

All new contracts live below `/api/v1`. Legacy servlet, Jersey, Axis, ISO 8583 and callback contracts stay owned by legacy until a consumer registry and contract test exist. A route can move only as a complete, versioned contract; implementation refactoring cannot silently change status codes, field names, content types, redirects or callback acknowledgment behavior.

## Consumer registry fields

Record owner, tenant scope, endpoint/version, authentication, source IP/network, request volume, retry policy, idempotency behavior, data classification, last-seen time and retirement contact. Unknown traffic blocks deprecation.

## Deprecation sequence

1. Measure and identify consumers.
2. Publish a versioned replacement and migration guide.
3. Run contract tests and parallel observability.
4. Announce a tenant-aware support window.
5. Return deprecation/sunset metadata where protocol-compatible.
6. Remove only after zero traffic and owner sign-off; retain route rollback during the agreed window.

Webhook/callback replacements require HMAC signatures, replay timestamps, idempotency keys, bounded payloads, safe error bodies and an audit record. OpenAPI is exposed at `/api/openapi.json`; generated documentation cannot replace consumer-owned contract tests.
