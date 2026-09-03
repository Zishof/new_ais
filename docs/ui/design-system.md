# AIS Next UI system

The UI uses pinned, self-hosted Tabler Core 1.4.0 and htmx 2.0.10. No production CDN or all-in-one generated HTML is used. Templates are server-rendered and remain usable when optional htmx enhancement is unavailable.

## Component contract

| Component | Template/CSS state | Accessibility rule |
|---|---|---|
| Shell/topbar/sidebar | `fragments/navigation.html`, `fragments/components.html` | Landmark elements and current-page indication |
| Breadcrumb | `fragments/components.html` | Ordered navigation with `aria-label` |
| Data table | roles list pattern | Caption/headers, horizontal overflow, server paging |
| Filter bar/drawer | roles query form; `fragments/components.html` | Explicit labels, keyboard close, preserved query |
| Lookup dialog | `fragments/components.html` | Native dialog semantics, focus return |
| Form section | `fragments/components.html` | Label association and summary of validation errors |
| Status | `fragments/status.html` | Text plus color; never color alone |
| Audit timeline | `fragments/components.html`, backed by audit port | Actor/time/action in semantic list |
| Empty/error/loading | status/component fragments and API problem response | Live message without focus theft |
| Confirmation/toast/live region | `fragments/components.html` | Destructive verbs explicit; `aria-live` region |

## Interaction rules

- Server controls validation, authorization, sorting, filtering and pagination.
- htmx responses return named fragments and preserve browser history where navigation changes.
- Buttons show loading/disabled state; errors stay near the control and also enter a live region.
- Responsive tables retain labels and do not encode meaning only by column position.
- Target WCAG 2.2 AA; Playwright + axe covers automated checks, followed by keyboard/screen-reader UAT.
