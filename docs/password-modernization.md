# Password modernization plan

1. Isolate the active legacy credential verifier behind `LegacyCredentialVerifier`; never expose stored hashes or accept them as bearer tokens.
2. On a valid legacy login, create or update a separate modern credential record using an adaptive password encoder (Argon2id or Spring Security's current delegated recommendation) while the original password is in memory.
3. Erase password character buffers promptly, rotate the session, audit the migration result without password/hash material, and rate-limit failures.
4. Prefer the modern store on later logins; fall back to legacy only while the account is unmigrated and policy allows it.
5. Provide a verified reset flow for accounts that never log in. Do not mass-convert hashes: the original password is unavailable.
6. Track algorithm/version and upgrade on successful login when policy changes. Support signing/pepper key rotation through secret references.
7. Disable plaintext/no-op paths only after usage telemetry, break-glass recovery and owner sign-off prove no dependent authentication channel remains.

The current code defines the ports but does not authenticate a password. The first slice relies on the signed one-time handoff so weak legacy material never crosses into AIS Next.
