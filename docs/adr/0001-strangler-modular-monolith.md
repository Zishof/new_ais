# ADR-0001: Modernize AIS with a strangler modular monolith

- Status: Accepted
- Date: 2026-09-03
- Owners: AIS architecture and platform teams

## Context

AIS legacy is an operational Java monolith with more than ten thousand JSP files, ZK views, Servlet 2.5-era integration, Spring Security 3.x, `javax.*` APIs, thousands of database tables, external callbacks, and long-lived business rules. Injecting Spring Boot 4/Jakarta libraries into that WAR creates an unsafe namespace and classpath migration. A big-bang rewrite would delay validation of hidden rules and make rollback coarse.

## Decision drivers

- Preserve operational continuity and existing contracts.
- Introduce Java 25, Spring Boot 4 and Spring Security 7 without destabilizing the WAR.
- Permit rollback per tenant, module, aggregate and route.
- Keep licensing open-source and deployment operationally simple.

## Decision

Build AIS Next as a separate executable JAR and modular monolith. Run it beside AIS legacy behind a tenant- and route-aware reverse proxy. Migrate capabilities incrementally using explicit route ownership and aggregate write ownership. The initial state is read-only; legacy remains the writer.

Do not introduce microservices until module boundaries and operational demand demonstrate a concrete need. Do not add modern dependencies to the legacy WAR.

## Alternatives considered

- Upgrade the legacy WAR in place: rejected because `javax`/Jakarta and container/framework generations collide across a very large surface.
- Big-bang rewrite: rejected because parity, cutover and rollback risks are unacceptable.
- Microservices from day one: rejected because it adds distributed transaction, deployment and observability cost before boundaries are proven.

## Consequences

Positive: incremental value, route-level rollback, modern security boundary, one deployable unit, and module seams that can later be extracted. Negative: two applications coexist temporarily, routing/ownership metadata becomes operationally critical, and parity tests are mandatory. The team must never imply that the scaffold equals full AIS completion.

## Revisit triggers

Revisit when a module has an independently scaling workload, separate release cadence, mature API/event contract, and measured benefit greater than distributed-system cost.
