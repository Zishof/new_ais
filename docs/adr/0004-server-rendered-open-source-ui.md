# ADR-0004: Use server-rendered Thymeleaf, htmx and Tabler OSS

- Status: Accepted
- Date: 2026-09-03
- Owners: AIS product and platform teams

## Context

AIS is form-, table- and workflow-heavy. The team needs a responsive modern interface without establishing a separate SPA deployment/toolchain or introducing commercial ERP widgets. Production cannot depend on public CDNs.

## Decision

Use Spring MVC with Thymeleaf for server-rendered pages, htmx for fragment interactions, and Tabler OSS/Bootstrap styling. Pin and self-host assets. Build reusable fragments for shell/navigation, status, tables, filters, forms, dialogs, audit timeline, empty/error/loading states, confirmation, toast and an accessibility live region. Keep authorization and validation on the server; the browser never sends SQL, class names or database identifiers.

Vaadin and premium UI artifacts are prohibited by the Maven dependency denylist unless a future ADR changes this decision after a licensing and memory review.

## Alternatives considered

- React/Vue SPA: not selected because the first slices do not justify an additional frontend platform and API duplication.
- Vaadin: not selected because commonly requested enterprise components may be commercial and server-side component state adds memory risk.
- Legacy JSP/ZK inside the new process: rejected because it carries forward the incompatible stack.

## Consequences

The solution remains one executable JAR and is usable with progressive enhancement. Rich client interactions require deliberate htmx patterns. Browser and accessibility tests remain mandatory. Current pinned assets are htmx 2.0.10 and Tabler Core 1.4.0.
