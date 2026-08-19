# ADR-0007: Linking Income/Expense Creation to Recurring Transactions

- **Status:** Accepted
- **Date:** 2026-08-17

## Context

`BaseTransactionRequest` (the shared parent of `IncomeRequest` and
`ExpenseRequest`) exposes `recurring` (Boolean) and `recurrencePattern`
(String) fields. `ServiceIncome.mapToEntity` and `ServiceExpense.mapToEntity`
persisted both values directly onto the `Income`/`Expense` row and stopped
there — no `RecurringTransaction` row was ever created.

This meant `recurring: true` on `POST /api/v1/incomes` or
`POST /api/v1/expenses` was purely informational: it painted a flag on the
transaction but never touched `RepositoryRecurringTransaction`, so
`RecurringExecutionService.executeDueTransactions()` (the scheduler entry
point, driven by `findDueTransactions`) had nothing to pick up. Only
transactions created explicitly through `POST /api/v1/recurring`
(`RecurringExecutionService.createRecurring`) actually recurred. Users had no
way to turn a normal income/expense into a recurring one from the same
request that created it, and no reliable way to locate the recurring
resource afterward to deactivate it via the existing
`PATCH /api/v1/recurring/{id}/deactivate`.

Wiring this up is not just "call the existing recurring service," because of
two structural constraints already present in the codebase:

1. **Circular bean dependency.** `RecurringTransactionExecutor` (used by
   `RecurringExecutionService` to materialize due transactions) already
   depends on `ServiceExpense` and `ServiceIncome` — it calls
   `serviceExpense.newExpense(...)` / `serviceIncome.newIncome(...)` when a
   recurring transaction fires. Injecting `RecurringExecutionService` into
   `ServiceExpense`/`ServiceIncome` to create the link from the POST side
   would form the cycle
   `ServiceExpense -> RecurringExecutionService -> RecurringTransactionExecutor -> ServiceExpense`,
   which Spring Boot (≥2.6, circular references disabled by default) refuses
   to start.

2. **Self-triggering loop.** When `RecurringTransactionExecutor` fires a due
   recurring transaction, it builds a request with `source="recurring"` and
   `recurring=true` and calls `newExpense`/`newIncome` again. Any logic that
   creates a `RecurringTransaction` whenever `recurring=true` — without
   excluding this case — would create a brand-new recurring template on
   every single execution of an existing one, compounding indefinitely.

## Decision

Introduce `RecurringLinkService`, a standalone bean in
`bflow.recurring.services` with **no dependency on
`RecurringTransactionExecutor` or `RecurringExecutionService`**, responsible
for creating/reconciling the `RecurringTransaction` template on behalf of
`ServiceIncome` and `ServiceExpense`. `RecurringExecutionService` is left
untouched and still owns the `/api/v1/recurring` CRUD and the scheduler
orchestration; `RecurringLinkService` only owns the create/update linking
logic triggered from the transaction endpoints.

`ServiceIncome`/`ServiceExpense` inject `RecurringLinkService` directly. This
keeps the dependency graph a DAG:

```
ServiceExpense ---> RecurringLinkService (leaf, no back-reference)
ServiceExpense <--- RecurringTransactionExecutor <--- RecurringExecutionService
```

**Create flow (`newIncome`/`newExpense`):** if `recurring=true` **and**
`source` is not `"recurring"`, call `RecurringLinkService.linkRecurring(...)`
with the already-resolved `wallet`/`category`/`contributor`, and store the
returned id on a new `recurring_transaction_id` column added to `incomes`
and `expenses` (migration `V17`). The `source != "recurring"` guard is what
prevents the self-triggering loop described above. `nextExecutionDate` is
set to *one interval after* the originating transaction's date, not the
date itself, since that transaction already represents the first
occurrence.

**Update flow (`updateIncome`/`updateExpense`):** `RecurringLinkService`
exposes `syncOnUpdate(...)`, which compares the entity's current recurring
state against the incoming request and only mutates the recurring template
when the change actually affects it:

| before → after | action |
|---|---|
| `false → false` | no-op, `recurringTransactionId` untouched |
| `false → true` | creates a new template |
| `true → false` | deactivates the existing template (same effect as `PATCH /deactivate`), clears the link |
| `true → true` | updates title/amount/wallet/category on the existing template; **recalculates `nextExecutionDate` only if `recurrencePattern` actually changed** |

Editing unrelated fields (description, tax flags, etc.) with `recurring`
left as `true` never touches the recurring schedule. This was an explicit
UX requirement: users editing a recurring transaction by mistake (wrong
amount, wrong category) should not silently reset or duplicate their
recurrence.

If a transaction is flagged `recurring=true` but its stored
`recurringTransactionId` doesn't resolve to a template owned by the same
user (data drift, e.g. rows created before this feature existed), the
service self-heals by creating a new template instead of throwing — a user
should never get blocked by inconsistent data they didn't cause.

`toggleRecurring`/`deleteRecurring` on `RecurringExecutionService` and the
existing `PATCH /api/v1/recurring/{id}/{activate,deactivate}` endpoints are
reused as-is; no new controller was needed for turning recurrence off.

## Alternatives considered

1. **Inject `RecurringExecutionService` directly into
   `ServiceIncome`/`ServiceExpense`.**
   Rejected: creates the circular bean dependency described in Context.
   Could be worked around with `@Lazy` injection, but that trades a clean
   dependency graph for a runtime proxy indirection to solve a design
   problem that a smaller, properly-scoped service solves for free.

2. **Have the income/expense controllers call
   `POST /api/v1/recurring` as a second request from the frontend,
   instead of doing it server-side.**
   Rejected: pushes an atomicity problem to the client (two requests, no
   shared transaction — a partial failure leaves the income/expense
   recurring-flagged with no backing template), and does not solve the
   original bug, which is that `recurring=true` on the transaction payload
   is silently ignored server-side.

3. **Merge the linking logic directly into
   `RecurringExecutionService` instead of a new service, breaking the
   cycle with `@Lazy` on the executor's dependencies instead.**
   Rejected: `@Lazy` on `RecurringTransactionExecutor`'s
   `ServiceExpense`/`ServiceIncome` fields would break the cycle, but
   couples two responsibilities (scheduler CRUD and transaction-triggered
   linking) that have different callers and different transactional
   requirements, and hides a design signal (the cycle) behind an
   annotation instead of removing it.

4. **Always overwrite the recurring template on every update where
   `recurring=true`, instead of diffing before/after.**
   Rejected per explicit UX requirement: an accidental or unrelated edit
   (e.g. fixing a typo in the description) would otherwise reset
   `nextExecutionDate`, silently shifting when the user's rent/subscription
   actually gets deducted.

## Consequences

**Positive:**
- `recurring=true` on `POST`/`PUT` for incomes and expenses now has an
  actual effect, closing the gap between the documented behavior and the
  observed one.
- No circular dependency introduced; `RecurringLinkService` is a leaf
  dependency, easy to unit test in isolation from the executor/scheduler.
- The self-triggering loop is structurally prevented by the `source`
  guard, not by a manual check callers have to remember.
- Update semantics protect users from accidentally corrupting their
  recurrence schedule while editing unrelated fields.

**Negative / trade-offs:**
- Two services now know how to build a `RecurringTransaction`
  (`RecurringExecutionService.createRecurring` for the explicit
  `/api/v1/recurring` flow, `RecurringLinkService` for the
  transaction-triggered flow). They are intentionally not unified to avoid
  reintroducing the cycle; any future change to how a `RecurringTransaction`
  is constructed (new required field, new validation) must be applied in
  both places.
- The self-heal path (creating a new template when the stored link is
  invalid) can mask data-integrity bugs by working around them instead of
  surfacing them. Acceptable for now given the low blast radius (a spare,
  active `RecurringTransaction` row), but should be revisited if it starts
  firing frequently in logs.
- `recurrencePattern` still accepts `YEARLY` at the DTO validation level
  (`BaseTransactionRequest`'s `@Pattern`) while `RecurringFrequency` does
  not define it, so `RecurringLinkService` rejects it at runtime with a
  clear `IllegalArgumentException` rather than failing silently. This
  pre-existing mismatch is not resolved by this ADR and should be its own
  follow-up (either extend `RecurringFrequency` or tighten the DTO pattern).
