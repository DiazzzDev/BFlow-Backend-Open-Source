# ADR-0006: Separation of `frequency` and `intervalValue` in Recurring Transactions

- **Status:** Accepted
- **Date:** 2026-08-13

## Context

The recurring transactions module (`RecurringTransaction`) needs to represent
how often an income or expense should execute (monthly rent, biweekly
subscription, savings every 3 days, quarterly payment, etc.).

The first approach considered was modeling the periodicity as a single closed
enum:

```java
enum RecurringFrequency {
    DAILY, WEEKLY, BIWEEKLY, MONTHLY, QUARTERLY, SEMIANNUAL, ANNUAL, ...
}
```

This approach has a scalability problem: every new cadence a user needs
(every 3 weeks, every 10 days, every 4 months) requires adding a new value to
the enum and extending all associated calculation logic (`switch`,
validations, frontend mappings). The enum grows without bound and never
covers every real-world case.

## Decision

Split periodicity into two independent fields:

- **`frequency`** (`RecurringFrequency`): defines the base **time unit**
  (`DAILY`, `WEEKLY`, `MONTHLY`).
- **`intervalValue`** (`Integer`, default `1`): defines the **multiplier**
  applied to that unit.

The next execution date is calculated by combining both fields:

```
next_execution_date = current_date + (intervalValue × unit_of(frequency))
```

Implemented in `RecurringExecutionService.updateNextExecution()`:

---

With `intervalValue = 1` by default (`DEFAULT_INTERVAL`), the behavior for a
"pure" frequency (daily, weekly, monthly) is unchanged from not having the
field at all.

This pattern mirrors the `INTERVAL` component of the iCalendar `RRULE`
specification (RFC 5545), also used by Google Calendar and Outlook for the
same purpose.

### Examples of resulting combinations

| frequency | intervalValue | Resulting cadence        |
|-----------|---------------|----------------------------|
| DAILY     | 1             | Every day                    |
| DAILY     | 3             | Every 3 days                  |
| WEEKLY    | 1             | Every week                    |
| WEEKLY    | 2             | Biweekly (every 2 weeks)      |
| MONTHLY   | 1             | Every month                    |
| MONTHLY   | 3             | Quarterly                       |
| MONTHLY   | 6             | Semiannual                       |

## Alternatives considered

1. **Extended enum covering every possible cadence**
   (`BIWEEKLY`, `QUARTERLY`, `SEMIANNUAL`, `EVERY_3_DAYS`, ...).
   Rejected: unbounded growth, requires a code release to cover cadences the
   business can't anticipate in advance.

2. **`cronExpression` field (cron string type)**
   Maximum flexibility, but over-engineered for the use case (only regular,
   simple cadences are needed) and complicates the end-user UI, which would
   have to build or interpret cron expressions.

3. **Single `days` field (everything expressed in days)**
   Simplifies the calculation, but breaks semantically for `MONTHLY` (months
   don't have a fixed length in days) and produces execution dates that drift
   over time (e.g., "every 30 days" is not the same as "every month").

## Consequences

**Positive:**
- Covers any regular cadence without touching the enum or the switch
  statement.
- Correct calendar-level date calculation (uses `plusMonths`, not raw day
  arithmetic), avoiding drift across months of different lengths.
- Simple validation: `intervalValue` is just a positive integer.

**Negative / trade-offs:**
- Does not support irregular or compound cadences (e.g., "every Monday and
  Thursday", "the 15th and the last day of the month"). If that's needed in
  the future, it would require a full `RRULE`-style model.
- The UI must expose two controls (unit + interval) instead of a single
  selector, which adds a small amount of cognitive load if not designed well
  (e.g., hiding the interval input when it equals `1`).
