# AIS Next UI/UX parity audit

## Outcome

AIS Next now meets the usable interaction baseline of the existing AIS application for the routes
that have actually moved to Next. It also improves responsive behavior, visual hierarchy,
keyboard focus, active-navigation feedback, and automated accessibility coverage. This is not a
claim that every Legacy screen has been migrated.

The live Legacy public portal and login at `http://localhost:18080/ais` were treated as reference
material, not as instructions. Authentication remains owned by Legacy: AIS Next links users to the
existing login and accepts only the signed, one-time handoff. It does not render a second password
form or retain a duplicate credential.

## Parity matrix

| User need | Existing AIS baseline | AIS Next implementation | Status |
|---|---|---|---|
| Recognize the product | Branded public portal and login card | Branded public entry and consistent authenticated shell | Met |
| Understand how to enter | Login is visibly primary | Primary `Masuk melalui AIS Legacy` action plus a three-step explanation | Met, safer handoff |
| Authenticate | Legacy ID/password and institutional providers | Delegated to Legacy; signed one-time handoff creates the Next session | Met by coexistence design |
| Find the main workspace | Authenticated landing/menu | Role-scoped dashboard with tenant, active role, and session context | Met |
| Discover available modules | Large Legacy menu surface | Compact service cards plus persistent primary navigation | Met for approved routes |
| Know the current location | Breadcrumb/menu state varies by page | Breadcrumb and automatic `aria-current="page"` state | Improved |
| Search | Search is available in the existing shell | Desktop and mobile global search controls | Met |
| Read and filter data | Forms and data tables | Consistent filter cards, tables, empty states, totals, and safety notices | Met |
| Use a phone or narrow window | Existing UI is primarily desktop-oriented | Collapsible full-width menu, stacked forms/cards, horizontally safe tables | Improved |
| Use keyboard or assistive technology | No automated acceptance gate was available | Skip link, visible focus, landmarks, labels, reduced motion, axe WCAG gate | Improved |
| Avoid accidental financial writes | Legacy exposes the complete operational surface | Account groups are explicitly read-only; journals and payments stay in Legacy | Improved safety |

## Design system applied

- A self-hosted Tabler foundation with AIS-specific color, spacing, type, focus, surface, and
  elevation tokens.
- A two-level desktop header so every permitted module remains visible, and a full-width collapsed
  menu for mobile.
- A modern public entry with one dominant action, concise security expectations, and no external
  asset dependency.
- A role-aware dashboard that displays only services authorized for the current handoff session.
- Shared visual treatment for page titles, breadcrumbs, filters, forms, tables, notices, totals,
  and status context across the migrated vertical slices.

## Delivery phases

| Phase | Acceptance criterion | Current state |
|---|---|---|
| 1. Baseline | Existing public, login, navigation, and data-page patterns are inventoried | Complete |
| 2. Design system | Reusable shell and shared responsive components exist | Complete |
| 3. Safe entry | Public entry returns to Legacy login and preserves the handoff boundary | Complete |
| 4. Authenticated home | Dashboard renders verified tenant, user, role, and permitted services | Complete |
| 5. Vertical-slice parity | Approved module pages use the shared shell without changing ownership | Complete for migrated routes |
| 6. Automated UAT | Desktop/mobile behavior and accessibility are enforced in Playwright | Complete |
| 7. Business UAT | Product owners accept wording, branding, workflows, and representative data | Pending owner sign-off |
| 8. Promotion | Each route passes security, operations, performance, and rollback gates | Pending per route |

## Intentional differences and remaining gates

AIS Next intentionally has no native password form while Legacy owns authentication. It also does
not copy every institution-specific shortcut, help widget, or non-migrated operational module.
Those features remain reachable through Legacy until their own contracts and route-ownership
decisions are approved.

Before production promotion, product owners still need to review institution branding, final
Indonesian wording, representative role combinations, browser support, and each module's workflow.
See the technical browser evidence in
[`docs/evidence/ui-ux-shell-uat-2026-09-04.md`](../evidence/ui-ux-shell-uat-2026-09-04.md).
