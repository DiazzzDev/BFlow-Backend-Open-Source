# Wompi Integration Notes

Operational reference for working with Wompi SV. For the architectural decisions behind this integration, see [ADR-0005: Wompi Payment Gateway Integration](./0005-wompi-payment-gateway.md).

## Tools to resolve issues

- **ngrok** — required for local webhook testing. Wompi does not support any alternative delivery mechanism (no polling fallback, no signed request replay from the dashboard) for the initial notification, only reconciliation via polling (see below).
- **Wompi merchant panel** (`panel.wompi.sv`) — used for: viewing `Servicios Recurrentes` subscriber lists, manually deactivating stale test links, and configuring the business-level webhook URL.
- **Postman / curl** — needed to inspect raw API responses before trusting a DTO shape. Do not assume Wompi's documented response shape matches reality; verify empirically (see Known Discrepancies below).

## Requirements

- The webhook endpoint for Wompi purchase notifications **must be publicly reachable**, even in development (ngrok tunnel or equivalent). Wompi will not retry indefinitely, and does not offer a sandbox-only delivery mode that bypasses this.
- The webhook route must be excluded from any authentication middleware (e.g. Spring Security's OAuth2 resource server config). Wompi does not authenticate as an application user — it will be silently rejected (401/403) by any filter chain that doesn't explicitly allow it.
- Billing-day calculations for recurring links must use `America/El_Salvador` time, not UTC or server-local time. A UTC-based calculation can misfire the billing day by up to 24 hours depending on time of day, which can cause a reconciliation job to expire a subscription before Wompi has even attempted the first real charge.

## Current limitations

- Stripe is currently not available in El Salvador and most Central American countries — Wompi is the only payment gateway with real local presence.
- Wompi SV does not support annual recurring subscriptions natively (`EnlacePagoRecurrente` only supports monthly cadence via `diaDePago`). Annual plans must be modeled as a one-time charge with backend-managed expiration and a manual renewal flow.
- Wompi SV documentation (`docs.wompi.sv`) requires updates; several sections are years out of date and multiple response shapes documented do not match the API's actual behavior (see Known Discrepancies).
- Recurring transactions (the actual charge execution) cannot be triggered or controlled by application code — Wompi's own scheduler decides when to attempt a charge based on the `diaDePago` set at link creation. The application can only react to the resulting webhook, or poll for status via reconciliation.
- No public refund/void endpoint exists in the Wompi SV API (confirmed by exhaustive review of the published Swagger spec). Refunds must be requested manually through Wompi support channels (chat, WhatsApp, support ticket, or email) — see Refunds section below.
- There are not enough public reference implementations for payment integrations with Wompi El Salvador (GitHub or otherwise) to rely on community patterns; most publicly available Wompi integration guides and sample code are written for Wompi Colombia, which is a materially different product (different endpoints, different auth flow, different recurrence model). Do not port patterns from Wompi Colombia without verifying them against `docs.wompi.sv` directly.

## Known discrepancies between documentation and actual API behavior

Found empirically during implementation; not reflected in the public docs at time of writing.

- `POST /EnlacePagoRecurrente` (create) and `GET /EnlacePagoRecurrente` (list) return different field names for the same resource: the former uses `idEnlace`/`urlEnlace`, the latter uses `id`/`urlCortaSuscribirse`.
- `POST /EnlacePagoRecurrente` can return `estaActivo: null` instead of a boolean, which breaks strict JSON deserialization if the response DTO isn't defensive about it.
- The recurring-charge webhook payload never populates `EnlacePago.Id` — only `Cliente.IdSuscripcion` is reliable for matching a recurring payment to a subscription.
- The webhook's email field is `EMail` (non-standard casing), not `Email` as a default `UpperCamelCase` naming strategy would produce.
- The business-level webhook configuration (`Mi Negocio` → `Notificar vía Webhook`) applies to both `EnlacePago` and `EnlacePagoRecurrente` — there is no separate webhook configuration per product, despite this not being explicitly documented.
- Wompi retries webhook delivery on 5xx responses (confirmed empirically — roughly 5 attempts within a short window) but does not retry on 4xx. A rejected webhook due to auth or malformed payload can be lost permanently without a reconciliation fallback.
- `POST /EnlacePago` (one-time link) accepts an optional `formaPago` object to restrict available payment methods at checkout (e.g. card-only, excluding Bitcoin/points); omitting it can surface payment method options the business isn't prepared to handle server-side.
- The business name and logo shown on the checkout screen come from the `Aplicativo`-level `nombre`/`urlLogo` fields (configurable in the merchant panel under "Mi Negocio"). If left blank, Wompi falls back to the personal account owner's name instead of the business name — this is not obvious from the panel UI and easy to miss.

## Refunds

There is no API to trigger a refund. The process is:

1. Merchant (BFlow) submits a request via Wompi's support channels (chat, WhatsApp, ticket, or `soporte@wompi.sv`), providing transaction details.
2. Wompi initiates the process with the acquiring entity on the merchant's behalf.
3. The acquiring entity may request supporting documentation (cardholder identification, proof of service delivery) within 5 business days of the request.
4. Funds may be held for up to 120 days during security monitoring or dispute investigation.
5. Wompi does not guarantee refund approval, and may charge an additional processing fee regardless of outcome.

Application-side, a refund is reflected as an admin-triggered state change (`Payment.status = REFUNDED`, subscription canceled with immediate effect) once BFlow confirms the refund was actually processed by Wompi — not at the moment the user requests it. See ADR-0005 for the reasoning behind this timing.

## Testing

- Wompi SV sandbox approves any transaction by default. To simulate a decline, submit `111` as the CVV — any other CVV value succeeds. There is no equivalent to Wompi Colombia's dedicated test card numbers; any Luhn-valid card number works.
- `EsProductiva: false` in the webhook payload distinguishes sandbox/test transactions from real ones — useful if reporting ever needs to filter these out before going fully live.
