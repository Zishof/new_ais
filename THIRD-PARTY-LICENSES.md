# Third-party software notices

This repository depends on open-source libraries managed by Maven and includes two pinned browser assets. `mvn verify` generates a CycloneDX SBOM with the exact transitive dependency graph; review it before release.

| Component | Version | License | Distribution |
|---|---:|---|---|
| htmx | 2.0.10 | 0BSD | Self-hosted minified asset; license copied beside it |
| Tabler Core | 1.4.0 | MIT | Self-hosted compiled CSS/JS; includes Bootstrap-based UI code |
| Bootstrap | 5.x bundled by Tabler | MIT | Transitive/bundled in Tabler distribution |
| Spring Boot / Framework / Security | 4.1.1 / managed | Apache-2.0 | Maven dependency |
| Spring Modulith | 2.1.1 | Apache-2.0 | Maven dependency |
| Hibernate ORM | Spring Boot managed | LGPL-2.1-or-later | Maven dependency when persistence is introduced |
| PostgreSQL JDBC | Spring Boot managed | BSD-2-Clause | Maven dependency |
| HikariCP | Spring Boot managed | Apache-2.0 | Maven dependency |
| Flyway Community | Spring Boot managed | Apache-2.0 | Maven dependency |
| Thymeleaf | Spring Boot managed | Apache-2.0 | Maven dependency |
| springdoc-openapi | 3.1.0 | Apache-2.0 | Maven dependency |
| Testcontainers | Spring Boot managed | MIT | Test dependency |
| JUnit / AssertJ / ArchUnit | Spring Boot/Modulith managed | EPL-2.0 / Apache-2.0 / Apache-2.0 | Test dependencies |
| Playwright Test | 1.62.1 | Apache-2.0 | Browser test dependency |
| axe-core Playwright | 4.13.0 | MPL-2.0 | Accessibility test dependency |

The allowlist is policy, not a substitute for legal review. GPL/AGPL, SSPL, BUSL, unknown or commercial licenses require explicit review and a separate decision before introduction.
