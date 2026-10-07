# Savings Monthly Table — Design Spec

**Date:** 2026-10-07
**Status:** Approved (design in chat), pending written-spec review
**Issue:** to be created before implementation
**Branch:** `feat/savings-monthly-table`

## 1. Goal

Add a `savings` table that stores one row per user per month with that month's
total income, total expense, and savings. The row is a read-only, derived
mirror of the user's transactions: it is recalculated on every transaction
change (insert, update, delete) and retroactively for the whole history by the
existing monthly scheduler.

## 2. Requirements (confirmed with user)

- **Formula:** `savings = totalIncome − totalExpense`, where each is the sum of
  the `price` field of transactions of that month grouped by `type`
  (INCOME / EXPENSE). Savings may be negative.
- **Recalculation (option 1 + 3 combined):** every insert, update, or delete of
  a transaction recalculates the affected month immediately; the scheduler
  retroactively recalculates any month that was never processed (full history).
- **Scope of retrospective recalc:** entire history (not capped to N months).
- **Empty months:** a month with no transactions must NOT have a row. If the
  last transaction of a month is deleted, the row is deleted.
- **Read-only:** the user cannot edit savings rows; there are no
  POST/PUT/DELETE endpoints. The row is always a calculation derived from
  transactions.
- **API:** `GET /savings` returns the full historical list for the
  authenticated user, ascending by (year, month).
- **Multi-user:** rows are scoped to the JWT user; unique constraint
  `(user_id, year, month)`.

## 3. Data model

New JPA entity `SavingsJpa`, table `savings`:

| Column | Type | Notes |
|---|---|---|
| `id` | bigint PK | `GenerationType.IDENTITY` |
| `user_id` | bigint FK → `users` | `@ManyToOne`, manual cascade like the rest of the schema |
| `year` | int | |
| `month` | int | 1–12 |
| `total_income` | double | |
| `total_expense` | double | |
| `savings` | double | `total_income − total_expense` |

**Unique constraint `(user_id, year, month)`** — enforces one row per month at
the database level and makes upserts safe.

Domain entity `Savings` (Lombok POJO, framework-free) and port
`SavingsRepository`.

> **Note for prod:** dev profile uses `ddl-auto=update`, so the table is
> created automatically. Prod uses `validate`; the DDL must be provided
> out-of-band when deploying (no migration tool in the project today).

## 4. Components

### 4.1 `SavingsService` (application port) + `SavingsServiceImp`

- `recalculate(User user, int year, int month)` — single source of truth for
  the calculation:
  1. Fetch the user's transactions for that month via a new
     `TransactionRepository.findByUserAndMonth(user, year, month)`.
  2. Sum `price` by `type` → income, expense.
  3. No transactions → delete the savings row if it exists.
  4. Otherwise → upsert (insert or update) the row.
- `recalculateAll()` — retrospective pass: for every user that has
  transactions, recalculate each month from their earliest transaction's
  month through the current month. Months with no transactions resolve via
  step 3 (delete if a row exists — a no-op for the common case where no row
  exists), so only months with transactions end up with rows.

Depends only on domain ports (`SavingsRepository`, `TransactionRepository`,
`Clock`) — no dependency on `TransactionService`, so no cycles.

### 4.2 Invocation points

**a) Transaction hooks — `TransactionServiceImp`:**

| Method | Recalculates |
|---|---|
| `createTransaction` | Month of the inserted date. If subtype is `FIXED` and backfill created past months: every month from the original month through the current month. |
| `updateTransaction` | Both the **old** month and the **new** month (the date may cross a month boundary). |
| `deleteTransaction` | Month of the deleted transaction's date. |

The service method performs the recalculation after the transaction write
inside the same `@Transactional` boundary.

**b) Scheduler — `RecurringJob.runMonthlyRecurrences()`:**

After `processFixedTransactions()` and `processAutomaticPayments()` (which
write transactions directly through the repository and therefore bypass the
hooks), call `savingsService.recalculateAll()`. This is the retrospective pass
and also covers transactions created by the job itself.

**c) User deletion — `UserRepositoryAdapter.delete`:**

The existing manual cascade (`payments → debts → transactions → categories →
user`) gains a `savings` step **before** `transactions` (FK safety).

### 4.3 API

`GET /savings` — authenticated (JWT), returns the full list for the current
user, ascending by year then month. No pagination in this iteration (MVP: a
user has few rows).

```json
[
  { "year": 2026, "month": 8, "totalIncome": 2500.0, "totalExpense": 1800.0, "savings": 700.0 },
  { "year": 2026, "month": 9, "totalIncome": 2500.0, "totalExpense": 2100.0, "savings": 400.0 }
]
```

New classes following existing patterns:
`SavingsController` → `SavingsService` → `SavingsResponseDTO`
(record: `year, month, totalIncome, totalExpense, savings`).

## 5. Data flow

```
insert/update/delete transaction
        │
        ▼
TransactionServiceImp ──► SavingsService.recalculate(user, y, m)
                                  │  reads transactions of month
                                  ▼
                          upsert / delete  ──► savings table

RecurringJob (day 1, 00:05)
        │
        ├── processFixedTransactions()      (writes via repository)
        ├── processAutomaticPayments()      (writes via repository)
        └── savingsService.recalculateAll() (retrospective, full history)

GET /savings ──► reads savings table only (no calculation on read)
```

## 6. Error handling

- Follows the project's `ExceptionHandlerController`: unexpected exceptions →
  500; no new exception types needed.
- `recalculate` on a user/month with no transactions and no existing row is a
  no-op (idempotent).
- Recalculation is idempotent: running it twice in the same state produces the
  same row.

## 7. Testing strategy

Unit tests (Mockito, same style as existing service tests):

- **`SavingsServiceImpTest`**
  - computes income/expense/savings from mixed transactions
  - upserts an existing row instead of creating a second one
  - deletes the row when the month has no transactions
  - no-op when month has no transactions and no row exists
  - `recalculateAll` covers every month with transactions through the current
    month
- **`TransactionServiceImpTest`** (extensions)
  - `createTransaction` calls `recalculate` with the inserted month
  - `createTransaction` FIXED backfill recalculates each backfilled month
  - `updateTransaction` with a date change recalculates both months
  - `deleteTransaction` recalculates the deleted month
- **`SavingsControllerTest`** — `@WebMvcTest`-style: returns list, requires
  auth, maps DTOs (mirrors `TransactionControllerTest`).

## 8. Delivery workflow (per AGENTS.md)

1. Create GitHub issue describing the change.
2. Branch `feat/savings-monthly-table` from `main`.
3. Implement with TDD (failing tests first), commit with `feat:` prefix.
4. Push and open PR referencing the issue (`Closes #N`).
5. **Do not merge** — user reviews and merges.

## 9. Out of scope (YAGNI)

- Pagination/filtering of `GET /savings`.
- Editing savings rows from the API.
- Migration tooling for prod DDL.
- Recalculating on read (approach B) or computing on the fly (approach C).
- Goals/targets for savings, trends, or charts.
