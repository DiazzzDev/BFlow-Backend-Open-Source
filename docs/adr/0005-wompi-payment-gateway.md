# ADR-0005: Wompi Payment Gateway Integration

## Context

BFlow needed a subscription monetization layer (Free and Pro plans, monthly and annual billing) for the El Salvador market. Wompi is the only payment gateway with meaningful local presence in the country; Stripe and other international gateways are not available there or in most of Central America.

## Decision

### 1. Wompi-hosted payment links, not server-side card tokenization

We chose Wompi's native payment link products (`EnlacePagoRecurrente` for monthly, `EnlacePago` for one-time/annual) over server-side card tokenization. This keeps card data entirely out of BFlow's infrastructure (out of PCI scope) and avoids relying on an unconfirmed detail — card token lifetime in Wompi SV, which we were never able to verify against documentation or support before this decision was made.

### 2. One recurring link per subscription, not one shared link per plan

Initially considered a single recurring link shared across all subscribers of a plan. Rejected because `diaDePago` (billing day) is fixed once at link creation and applies to every subscriber of that link — a user subscribing shortly before the configured billing day would get up to a full month free before their first real charge.

Each `Subscription` now creates its own `EnlacePagoRecurrente` at checkout time, with `diaDePago` computed from the user's signup date. This also enables true per-subscriber cancellation (deactivating one link never affects other subscribers) and removes ambiguity from webhook matching.

### 3. Annual plan as a one-time charge, not a recurring link

Wompi SV's recurring link product has no annual cadence option. The annual plan is implemented as a single `EnlacePago` charge, with `endsAt`/`nextBillingAt` computed and tracked entirely in BFlow (+365 days from activation), plus a reminder email sent ahead of expiration. There is no automatic renewal charge — the user must complete a new checkout when reminded.

### 4. Feature/plan-limit model as a generic entitlements table, not fixed columns on `Plan`

Considered flat limit columns directly on `Plan` (already populated with real data at the time of the decision) versus a generic `Feature`/`PlanFeature` model. Chose the generic model because the product's benefit catalog (~10 entitlements and growing) makes adding a new column per limit a poor long-term fit. Existing flat-column data was migrated into the new model; a centralized `PlanLimitService` is the single point of enforcement for both numeric limits and boolean toggles across all gated resources (budgets, wallets, recurring transactions, wallet sharing).

### 5. Soft-lock on downgrade, not active pruning

When a user downgrades (voluntary cancellation, payment failure expiry, or refund), existing resources that exceed the new plan's limits are neither deleted nor hidden. Only creation of new resources is blocked until the user is back under their current limit. This avoids destructive behavior on downgrade and requires no cleanup logic — enforcement always checks against current plan state, never a historical snapshot.

### 6. Active reconciliation against lost webhooks

Because Wompi does not retry webhook delivery on 4xx responses (see Integration Notes) and a missed webhook would otherwise leave a subscription stuck indefinitely, a scheduled reconciliation job polls Wompi's read-only subscriber list for any subscription still pending activation past a grace period. It activates on confirmed payment, expires on no match, and has a hard cutoff to avoid retrying indefinitely on genuinely abandoned checkouts.

### 7. Refund handling is a manual, admin-confirmed state transition

Wompi SV exposes no refund API (see Integration Notes). A refund is therefore represented in BFlow only as an explicit admin action once the refund has been *confirmed processed* by Wompi — not at the moment the user requests one. Premium access is deactivated immediately at that point, with no grace period, since the paid period the access was based on has itself been reversed. This was a deliberate choice among three options considered (cancel on request, keep access until period end regardless of refund, deactivate on confirmed refund); the first risks revoking access before a refund is guaranteed, the second grants free service after the payment is undone, the third is the only internally consistent option.

## Consequences

**Positive:**
- No PCI scope exposure — BFlow never touches raw card data.
- True per-subscriber cancellation without affecting other users on the same plan.
- Automatic recovery from lost webhooks via scheduled reconciliation.
- Entitlement model extends to new plan benefits without schema migrations.

**Negative / accepted limitations:**
- No automatic renewal for the annual plan; depends on the user completing a new manual checkout after a reminder.
- No automatable refund path; every refund depends on Wompi's manual support process, with turnaround and fund-hold timelines outside BFlow's control.

## Open follow-ups

- Confirm with Wompi support the exact refund mechanism and timelines beyond same-day voiding, if one exists.
