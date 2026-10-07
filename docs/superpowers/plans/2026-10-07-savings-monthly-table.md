# Savings Monthly Table Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `savings` table with one derived row per user per month (total income, total expense, savings), recalculated on every transaction insert/update/delete and retroactively for the whole history by the existing monthly scheduler.

**Architecture:** New hexagonal vertical slice: `Savings` domain entity + `SavingsRepository` port, `SavingsService` port with `SavingsServiceImp.recalculate(user, year, month)` / `recalculateAll()` as the single calculation source of truth, JPA adapter with a unique `(user_id, year, month)` constraint, read-only `GET /savings` controller. Recalculation is invoked from three points: hooks in `TransactionServiceImp` (create/update/delete), `RecurringJob` (retrospective pass), and the user-deletion cascade.

**Tech Stack:** Spring Boot 4.1.0, Java 25, PostgreSQL + Spring Data JPA, Lombok, Mockito unit tests + `@WebMvcTest`.

**Spec:** `docs/superpowers/specs/2026-10-07-savings-monthly-table-design.md`

## Global Constraints

- Hexagonal layering: `domain` never imports `application` or `infrastructure`; domain stays framework-free (only `java.*` and `lombok.*`).
- Never import `infrastructure` from `domain` or `application`.
- Follow existing naming: services `*ServiceImp`, ports in `application/ports/`, JPA repos `Postgres*`, adapters in `infrastructure/persistance/adapter/`, mappers `*JpaMapper` (infrastructure) and `*Mapper` (application DTO ↔ domain).
- All money sums use the `price` field (`Double`) of `Transaction`; `amount` is a quantity, not used in savings math.
- Savings row is read-only: no POST/PUT/DELETE endpoints for it.
- A month with zero transactions must have NO savings row (delete the row if it exists).
- Unique constraint `(user_id, year, month)` in the database.
- AGENTS.md workflow: new issue, branch from latest `main`, `feat:` commit prefixes, PR references the issue, **do not merge**.
- Commit identity is not configured globally in this environment: prefix git commit commands with `git -c user.email="bot@opencode.local" -c user.name="Opencode Bot"`.
- Build: `mvn compile`; tests: `mvn test` (PostgreSQL required on localhost:5432). Test selection: `mvn test -Dtest=ClassName`.

## Review Focus

- **Update that moves a transaction across month boundaries** — the old month's totals must drop it and the new month's must gain it; both rows recalculated. Owned by Task 6 (`updateTransaction` recalcutes both old and new month).
- **Deleting the last transaction of a month** — the savings row for that month must be deleted, not left with stale totals. Owned by Task 3 (`recalculate` deletes on empty) + Task 6 (delete hook).
- **Scheduler-created transactions bypass `TransactionService`** — `RecurringServiceImp` writes via repository directly; the retrospective `recalculateAll()` in `RecurringJob` must cover those months. Owned by Task 7 (and integration is verified by an assertion that `RecurringJob` calls `recalculateAll()`).
- **User deletion FK order** — `savings.user_id` FK must be cleared before `users` row is deleted, alongside the existing manual cascade. Owned by Task 8 (test asserts `jpaSavings.deleteByUser_Id` is called before `jpa.deleteById`).
- **Concurrent/duplicate recalculation** — upsert keyed by `(user, year, month)` must not create a second row when recalculating twice; DB unique constraint is the backstop, service does lookup-then-save. Owned by Task 3 (test: recalculating twice keeps one row).

---

### Task 1: Domain entity + ports (`Savings`, `SavingsRepository`, `TransactionRepository.findByUserAndMonth`, `SavingsService`)

**Files:**
- Create: `src/main/java/com/money/manager/domain/Savings.java`
- Create: `src/main/java/com/money/manager/domain/SavingsRepository.java`
- Create: `src/main/java/com/money/manager/application/ports/SavingsService.java`
- Modify: `src/main/java/com/money/manager/domain/TransactionRepository.java`

**Interfaces:**
- Produces (relied on by later tasks):
  - `Savings` Lombok POJO: `Long id; User user; int year; int month; double totalIncome; double totalExpense; double savings;` with `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder` (same style as `Debt`).
  - `SavingsRepository` (domain port): `List<Savings> findByUser(User user);` `Optional<Savings> findByUserAndYearAndMonth(User user, int year, int month);` `Savings save(Savings savings);` `void delete(Savings savings);` `void deleteByUser_Id(Long userId);`
  - `TransactionRepository` gains: `List<Transaction> findByUserAndMonth(User user, int year, int month);`
  - `SavingsService` port is NOT created here — it is declared in Task 3 (with all three methods; `getSavings` returns `List<Object>`-free shape declared in Task 5). Task 1 produces only entities and repository ports.

- [ ] **Step 1: Create `Savings.java` domain entity**

Exact fields and annotations as in the Produces block above. Package `com.money.manager.domain`.

- [ ] **Step 2: Create `SavingsRepository.java` domain port**

Exact signatures as in the Produces block. Imports `java.util.List`, `java.util.Optional` only — framework-free.

- [ ] **Step 3: Add `findByUserAndMonth` to `TransactionRepository`**

```java
List<Transaction> findByUserAndMonth(User user, int year, int month);
```

- [ ] **Step 4: Compile to verify**

Run: `mvn compile`
Expected: BUILD SUCCESS (interface additions compile; adapters not yet required to implement — if the adapter fails to compile because it implements the interface, add a temporary `UnsupportedOperationException` stub and note it; Task 2 implements it for real. Same for `SavingsRepository` — no adapter exists yet, port alone compiles.)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/money/manager/domain/Savings.java src/main/java/com/money/manager/domain/SavingsRepository.java src/main/java/com/money/manager/domain/TransactionRepository.java
git -c user.email="bot@opencode.local" -c user.name="Opencode Bot" commit -m "feat: add Savings domain entity and repository ports"
```

---

### Task 2: Persistence (JPA entity, Spring Data repo, mapper, adapter) + `findByUserAndMonth` implementation

**Files:**
- Create: `src/main/java/com/money/manager/infrastructure/persistance/entity/SavingsJpa.java`
- Create: `src/main/java/com/money/manager/infrastructure/persistance/PostgresSavingsRepository.java`
- Create: `src/main/java/com/money/manager/infrastructure/persistance/mapper/SavingsJpaMapper.java`
- Create: `src/main/java/com/money/manager/infrastructure/persistance/adapter/SavingsRepositoryAdapter.java`
- Modify: `src/main/java/com/money/manager/infrastructure/persistance/PostgresTransactionRepository.java`
- Modify: `src/main/java/com/money/manager/infrastructure/persistance/adapter/TransactionRepositoryAdapter.java`
- Test: `src/test/java/com/money/manager/infrastructure/persistance/adapter/SavingsRepositoryAdapterTest.java`

**Interfaces:**
- Consumes: `Savings`, `SavingsRepository`, `TransactionRepository.findByUserAndMonth` (Task 1).
- Produces:
  - `SavingsJpa` — `@Table(name = "savings", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "year", "month"}))`, fields: `Long id` (IDENTITY), `int year`, `int month`, `double totalIncome`, `double totalExpense`, `double savings`, `@ManyToOne @JoinColumn(name = "user_id") UserJpa user`. Lombok annotations like `PaymentJpa`.
  - `PostgresSavingsRepository extends JpaRepository<SavingsJpa, Long>` with `List<SavingsJpa> findByUser_IdOrderByYearAscMonthAsc(Long userId);` `Optional<SavingsJpa> findByUser_IdAndYearAndMonth(Long userId, int year, int month);` `void deleteByUser_Id(Long userId);`
  - `SavingsJpaMapper.toJpa(Savings, UserJpa) -> SavingsJpa` and `SavingsJpaMapper.toDomain(SavingsJpa) -> Savings` (static methods, mirror `PaymentJpaMapper`; domain `user` built as `User.builder().id(jpa.getUser().getId()).build()`).
  - `SavingsRepositoryAdapter implements SavingsRepository` — `@Component @RequiredArgsConstructor`, `@Transactional` on write methods, `@Transactional(readOnly = true)` on reads, resolves `UserJpa` via injected `PostgresUserRepository.findById` like `PaymentRepositoryAdapter` resolves `DebtJpa`.
  - `PostgresTransactionRepository` gains `findByUser_IdAndYearAndMonth` via `@Query` using `EXTRACT(YEAR/MONTH FROM t.dateTransaction)` (copy the style of the existing `existsByUserCategoryNameAmountTypeSubtypeAndMonth` query); `TransactionRepositoryAdapter.findByUserAndMonth` maps with `TransactionJpaMapper::toDomain`.

- [ ] **Step 1: Write the failing adapter test**

`SavingsRepositoryAdapterTest` (Mockito, style of existing adapter tests): construct adapter with mocked `PostgresSavingsRepository` + `PostgresUserRepository`. Assert:
  - `findByUser` calls `jpa.findByUser_IdOrderByYearAscMonthAsc(1L)` and maps rows to domain (mapper stub via `when(...).thenReturn(List.of(jpaRow))`, assert returned domain list has expected year/month).
  - `findByUserAndYearAndMonth` resolves the user first (`jpaUser.findById(1L)` returns a `UserJpa`) then calls `jpa.findByUser_IdAndYearAndMonth(...)`.
  - `save` resolves `UserJpa` then calls `jpa.save(...)`.
  - `deleteByUser_Id` calls `jpa.deleteByUser_Id(1L)`.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=SavingsRepositoryAdapterTest`
Expected: FAIL (class not found)

- [ ] **Step 3: Implement the four persistence classes**

As specified in the Produces block.

- [ ] **Step 4: Implement `findByUserAndMonth` on the transaction side**

JPQL `@Query` on `PostgresTransactionRepository`:
```java
@Query("""
    SELECT t FROM TransactionJpa t
    WHERE t.user = :user
      AND EXTRACT(YEAR FROM t.dateTransaction) = :year
      AND EXTRACT(MONTH FROM t.dateTransaction) = :month
""")
List<TransactionJpa> findByUser_IdAndYearAndMonth(@Param("user") UserJpa user, @Param("year") int year, @Param("month") int month);
```
`TransactionRepositoryAdapter.findByUserAndMonth`: resolve `UserJpa` from `UserJpaMapper.toJpa(user)` (or `jpaUser.findById`) — follow how the adapter resolves parents today (`existsBy...` resolves via `PostgresUserRepository.findById` in `TransactionRepositoryAdapter`; mirror that), then map with `TransactionJpaMapper::toDomain`.

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn test -Dtest=SavingsRepositoryAdapterTest`
Expected: PASS

- [ ] **Step 6: Compile everything**

Run: `mvn compile`
Expected: BUILD SUCCESS (Task 1 stubs, if any, now implemented)

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/money/manager/infrastructure/persistance src/test/java/com/money/manager/infrastructure/persistance/adapter/SavingsRepositoryAdapterTest.java
git -c user.email="bot@opencode.local" -c user.name="Opencode Bot" commit -m "feat: add savings persistence adapter and findByUserAndMonth query"
```

---

### Task 3: `SavingsServiceImp.recalculate` (calculation core)

**Files:**
- Create: `src/main/java/com/money/manager/application/services/SavingsServiceImp.java`
- Test: `src/test/java/com/money/manager/application/services/SavingsServiceImpTest.java`

**Interfaces:**
- Consumes: `SavingsRepository.findByUserAndYearAndMonth/save/delete`, `TransactionRepository.findByUserAndMonth`, `Clock` bean (`TimeConfig`).
- Produces: `SavingsService` port (`application/ports/SavingsService.java`) declared with `recalculate(User, int, int)` and `recalculateAll()`; `SavingsServiceImp implements SavingsService` — implement `recalculate` here, `recalculateAll` throws `UnsupportedOperationException("not implemented")` until Task 7. (`getSavings` is added to the port in Task 5 when the DTO exists.) Signature: `void recalculate(User user, int year, int month)`.

Calculation algorithm (the body the tests pin):
1. `List<Transaction> txs = transactionRepository.findByUserAndMonth(user, year, month);`
2. `totalIncome = sum of price where type == Type.INCOME; totalExpense = sum where type == Type.EXPENSE;` (null price treated as 0.0)
3. If `txs.isEmpty()` → if a row exists (`findByUserAndYearAndMonth`) → `delete` it; return.
4. Else build/upsert: fetch existing row; if present, update `totalIncome/totalExpense/savings`; else `save(Savings.builder()...user...year...month...build())`. Set `savings = totalIncome - totalExpense`.

- [ ] **Step 1: Write the failing test — `SavingsServiceImpTest`**

Mockito style like `RecurringServiceImpTest`. Tests:
  - `recalculate_computesIncomeExpenseAndSavings` — transactions: INCOME price 2500.0, EXPENSE price 1800.0, EXPENSE price 200.0 (same month, different days); no existing row (`findByUserAndYearAndMonth` empty); verify `save` called with Savings having `totalIncome=2500.0`, `totalExpense=2000.0`, `savings=500.0`, correct user/year/month.
  - `recalculate_deletesRowWhenMonthHasNoTransactions` — `findByUserAndMonth` returns empty, existing row present → verify `delete(existing)` and `save` never called.
  - `recalculate_noopWhenEmptyMonthAndNoRow` — empty txs, no row → verify no `save`, no `delete`.
  - `recalculate_updatesExistingRowInsteadOfInserting` — existing row with stale values, new totals → verify `save` called on the SAME object (same id) with updated values.
  - `recalculate_twiceProducesSingleRow` — second call finds existing row via `findByUserAndYearAndMonth` and updates it (mock returns the saved row on second call) → verify only one logical row ever built (id stable).

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=SavingsServiceImpTest`
Expected: FAIL (class not found)

- [ ] **Step 3: Implement `SavingsServiceImp.recalculate`**

As per the algorithm. Constructor: `@RequiredArgsConstructor` with `SavingsRepository savingsRepository; TransactionRepository transactionRepository; Clock clock;`. Declare port `SavingsService` in `application/ports` with `recalculate` and `recalculateAll`; implement `recalculate` and throw `UnsupportedOperationException("not implemented")` in `recalculateAll` (replaced in Task 7).

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=SavingsServiceImpTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/money/manager/application/ports/SavingsService.java src/main/java/com/money/manager/application/services/SavingsServiceImp.java src/test/java/com/money/manager/application/services/SavingsServiceImpTest.java
git -c user.email="bot@opencode.local" -c user.name="Opencode Bot" commit -m "feat: add SavingsService.recalculate core calculation"
```

---

### Task 4: `TransactionRepositoryAdapter.findByUserAndMonth` test coverage

**Files:**
- Test: `src/test/java/com/money/manager/infrastructure/persistance/adapter/TransactionRepositoryAdapterTest.java` (create if absent)

**Interfaces:**
- Consumes: `TransactionRepositoryAdapter.findByUserAndMonth(User, int, int)` (Task 2).

The savings calculation depends entirely on this query returning the right transactions, and Task 2 only verified it compiles. This task pins its adapter behavior.

- [ ] **Step 1: Write the failing test**

Mockito test (style of `SavingsRepositoryAdapterTest` from Task 2): mocked `PostgresTransactionRepository` + `PostgresUserRepository`. Given `jpaUser.findById(1L)` returns a `UserJpa` and `jpa.findByUser_IdAndYearAndMonth(userJpa, 2026, 9)` returns two `TransactionJpa` rows (one INCOME price 2500.0 dated 2026-09-05, one EXPENSE price 1800.0 dated 2026-09-20), assert `adapter.findByUserAndMonth(user, 2026, 9)` returns 2 domain transactions with matching prices and dates.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=TransactionRepositoryAdapterTest`
Expected: FAIL if Task 2's implementation is wrong/missing (if it passes because Task 2 already did it correctly, that is an acceptable pass — the test still adds the pin; note it in the commit message)

- [ ] **Step 3: Fix implementation if the test exposed a bug; otherwise leave code unchanged**

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=TransactionRepositoryAdapterTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/money/manager/infrastructure/persistance/adapter/TransactionRepositoryAdapterTest.java
git -c user.email="bot@opencode.local" -c user.name="Opencode Bot" commit -m "test: pin findByUserAndMonth adapter behavior"
```

---

### Task 5: DTO + `SavingsService.getSavings` + `GET /savings` controller

**Files:**
- Create: `src/main/java/com/money/manager/application/dtos/SavingsResponseDTO.java`
- Create: `src/main/java/com/money/manager/application/mappers/SavingsMapper.java`
- Create: `src/main/java/com/money/manager/infrastructure/controller/SavingsController.java`
- Modify: `src/main/java/com/money/manager/application/services/SavingsServiceImp.java`
- Modify: `src/main/java/com/money/manager/application/ports/SavingsService.java` (add `List<SavingsResponseDTO> getSavings(User user);`)
- Test: `src/test/java/com/money/manager/infrastructure/controller/SavingsControllerTest.java`

**Interfaces:**
- Consumes: `SavingsRepository.findByUser`, `Savings` (Task 1/2).
- Produces:
  - `SavingsResponseDTO` record: `int year, int month, double totalIncome, double totalExpense, double savings` (package `application.dtos`).
  - `SavingsMapper.toDto(Savings) -> SavingsResponseDTO` static method.
  - `SavingsService.getSavings(User user) -> List<SavingsResponseDTO>` — ordered ascending (repository already orders).
  - `SavingsController` — `@RestController @RequestMapping("savings")`, single `@GetMapping("")` returning `ResponseEntity<List<SavingsResponseDTO>>`, user from `(User) authentication.getPrincipal()` exactly like `TransactionController`.

- [ ] **Step 1: Write the failing controller test**

`SavingsControllerTest` copied in structure from `TransactionControllerTest` (`@WebMvcTest(controllers = SavingsController.class, excludeFilters = ... JwtFilter, RateLimiterFilter ...)`, permissive `SecurityFilterChain` test config, `@MockitoBean SavingsService`, authentication post-processor). Tests:
  - `getSavings_returnsListAscending` — service returns two DTOs (2026-08 savings 700.0, 2026-09 savings 400.0) → expect 200, `jsonPath("$[0].month").value(8)`, `jsonPath("$[0].savings").value(700.0)`, `jsonPath("$[1].month").value(9)`.
  - `getSavings_requiresAuthentication` — request without authentication → expect 401/403 (whichever the project's test security config produces; mirror how existing controller tests assert unauthenticated access — if none do, assert `status().isUnauthorized()`; verify against `SecurityConfig`).

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=SavingsControllerTest`
Expected: FAIL (controller not found)

- [ ] **Step 3: Implement DTO, mapper, service method, controller**

As per Produces. `getSavings` body: `savingsRepository.findByUser(user).stream().map(SavingsMapper::toDto).toList()`.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=SavingsControllerTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/money/manager/application/dtos/SavingsResponseDTO.java src/main/java/com/money/manager/application/mappers/SavingsMapper.java src/main/java/com/money/manager/infrastructure/controller/SavingsController.java src/main/java/com/money/manager/application src/test/java/com/money/manager/infrastructure/controller/SavingsControllerTest.java
git -c user.email="bot@opencode.local" -c user.name="Opencode Bot" commit -m "feat: add GET /savings endpoint"
```

---

### Task 6: Hooks in `TransactionServiceImp` (create / update / delete / backfill range)

**Files:**
- Modify: `src/main/java/com/money/manager/application/services/TransactionServiceImp.java`
- Test: `src/test/java/com/money/manager/application/services/TransactionServiceImpTest.java`

**Interfaces:**
- Consumes: `SavingsService.recalculate(User, int, int)` (Task 3).
- Produces: `TransactionServiceImp` gains constructor param `SavingsService savingsService` (`@RequiredArgsConstructor` — all existing tests constructing it manually must add the mock).

Recalculation points (exact):
- `createTransaction`: after save (and after `backfillFixedTransactions` if FIXED), recalc the month of `transaction.getDateTransaction()`. If FIXED with backfill, also recalc each month from original month through current month — the backfill loop already iterates those months; simplest correct implementation: recalc `originalMonth` and every cursor month. Implement by collecting months during backfill or recalc original month + loop months from `YearMonth.from(original)` to current month.
- `updateTransaction`: capture `oldMonth = YearMonth.from(transaction.getDateTransaction())` BEFORE mutating; after save, recalc `oldMonth` and `YearMonth.from(newDate)`.
- `deleteTransaction`: recalc month of the transaction's date BEFORE `delete` (or after — month is known from the object either way).

- [ ] **Step 1: Write the failing tests (extend `TransactionServiceImpTest`)**

  - `createTransaction_recalculatesInsertedMonth` — non-FIXED transaction dated `2026-09-15` → verify `savingsService.recalculate(user, 2026, 9)` called once.
  - `createTransaction_fixedBackfill_recalculatesEachBackfilledMonth` — FIXED dated `2026-07-10`, clock at `2026-09-15` → verify `recalculate` called for (2026,7), (2026,8), (2026,9).
  - `updateTransaction_dateCrossesMonth_recalculatesBothMonths` — existing transaction dated `2026-09-15`, update sets date `2026-10-05` → verify `recalculate(user,2026,9)` AND `recalculate(user,2026,10)`.
  - `updateTransaction_sameMonth_recalculatesOnce` — date stays in same month → verify `recalculate` called once with that month.
  - `deleteTransaction_recalculatesDeletedMonth` — transaction dated `2026-09-15` deleted → verify `recalculate(user,2026,9)`.

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=TransactionServiceImpTest`
Expected: FAIL (no `savingsService` interactions / constructor mismatch)

- [ ] **Step 3: Implement the hooks**

Add `private final SavingsService savingsService;` field; add the recalculation calls at the three methods per Produces. All calls inside the existing `@Transactional` methods.

- [ ] **Step 4: Run tests to verify they pass (all of them)**

Run: `mvn test -Dtest=TransactionServiceImpTest`
Expected: PASS (existing tests updated with the new mock argument)

- [ ] **Step 5: Run the full suite**

Run: `mvn test`
Expected: all tests pass (only pre-existing skips allowed)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/money/manager/application/services/TransactionServiceImp.java src/test/java/com/money/manager/application/services/TransactionServiceImpTest.java
git -c user.email="bot@opencode.local" -c user.name="Opencode Bot" commit -m "feat: recalculate savings on transaction create/update/delete"
```

---

### Task 7: Scheduler retrospective — `recalculateAll()` + `RecurringJob` wiring

**Files:**
- Modify: `src/main/java/com/money/manager/application/services/SavingsServiceImp.java`
- Modify: `src/main/java/com/money/manager/infrastructure/scheduler/RecurringJob.java`
- Test: `src/test/java/com/money/manager/application/services/SavingsServiceImpTest.java` (extend)

**Interfaces:**
- Consumes: `TransactionRepository.findByUserAndMonth`, `SavingsService.recalculate`, `Clock`.
- Produces: `SavingsService.recalculateAll()` — for every user that has transactions: months from user's earliest transaction month through current month; call `recalculate(user, y, m)` for each. Needs: earliest month per user. Use existing `TransactionRepository.findByUser(user)` (already exists) → compute min date month → iterate to current month. `RecalculateAll` implementation: fetch distinct users with transactions — no such query exists; simplest: no global user list in `TransactionRepository`. **Decision:** add `List<User> findUsersWithTransactions()` port method (`@Query SELECT DISTINCT t.user FROM TransactionJpa t`) implemented in Task 2's adapter style — **added here as a Modify of `TransactionRepository` + `PostgresTransactionRepository` + `TransactionRepositoryAdapter`.**

- [ ] **Step 1: Write the failing test (extend `SavingsServiceImpTest`)**

  - `recalculateAll_coversEveryMonthFromEarliestToCurrent` — mocked: `findUsersWithTransactions()` returns one user; `findByUser(user)` returns one transaction dated `2026-08-20`; clock at `2026-10-07` → verify `recalculate` called with (2026,8), (2026,9), (2026,10).
  - `recalculateAll_skipsUsersWithoutTransactions` — `findUsersWithTransactions()` returns empty → verify `recalculate` never called.
  - `RecurringJob` wiring test (new tiny test `RecurringJobTest`): mock `RecurringService` + `SavingsService`, call `runMonthlyRecurrences()`, verify `recalculateAll()` called after both recurring processes.

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -Dtest=SavingsServiceImpTest,RecurringJobTest`
Expected: FAIL

- [ ] **Step 3: Implement `findUsersWithTransactions` + `recalculateAll` + job wiring**

Port method on `TransactionRepository`: `List<User> findUsersWithTransactions();` — JPQL `SELECT DISTINCT t.user FROM TransactionJpa t`. `recalculateAll` body: for each user → `findByUser(user)` → min `dateTransaction` → `YearMonth` cursor from that to `YearMonth.now(clock)` → `recalculate(user, y.getYear(), y.getMonthValue())`. Remove last `UnsupportedOperationException` stub. `RecurringJob.runMonthlyRecurrences()`: call `recurringService.processFixedTransactions(); recurringService.processAutomaticPayments(); savingsService.recalculateAll();`

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -Dtest=SavingsServiceImpTest,RecurringJobTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java src/test/java
git -c user.email="bot@opencode.local" -c user.name="Opencode Bot" commit -m "feat: add retrospective savings recalculation to monthly job"
```

---

### Task 8: User deletion cascade includes savings

**Files:**
- Modify: `src/main/java/com/money/manager/infrastructure/persistance/adapter/UserRepositoryAdapter.java`
- Test: `src/test/java/com/money/manager/infrastructure/persistance/adapter/UserRepositoryAdapterTest.java`

**Interfaces:**
- Consumes: `SavingsRepository` adapter / `PostgresSavingsRepository.deleteByUser_Id` (Task 2).
- Produces: `UserRepositoryAdapter.delete` calls `jpaSavings.deleteByUser_Id(user.getId())` **before** `jpaTransaction.deleteByUser_Id(...)`.

- [ ] **Step 1: Write the failing test (extend existing `UserRepositoryAdapterTest`)**

  - `delete_removesSavingsBeforeTransactions` — verify `jpaSavings.deleteByUser_Id(1L)` invoked; use `InOrder` to assert it happens before `jpaTransaction.deleteByUser_Id(1L)` (mirrors existing cascade tests if present; otherwise `verify`).

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=UserRepositoryAdapterTest`
Expected: FAIL

- [ ] **Step 3: Implement**

Add `private final PostgresSavingsRepository jpaSavings;` + the call in `delete`.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=UserRepositoryAdapterTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/money/manager/infrastructure/persistance/adapter/UserRepositoryAdapter.java src/test/java/com/money/manager/infrastructure/persistance/adapter/UserRepositoryAdapterTest.java
git -c user.email="bot@opencode.local" -c user.name="Opencode Bot" commit -m "feat: delete savings rows in user deletion cascade"
```

---

### Task 9: Full verification + issue + PR

**Files:**
- None new (verification + delivery).

- [ ] **Step 1: Full test suite**

Run: `mvn test`
Expected: all green, no new skips.

- [ ] **Step 2: Package build**

Run: `mvn clean package -DskipTests -o`
Expected: BUILD SUCCESS (offline flag works once deps cached; drop `-o` if it fails on missing deps).

- [ ] **Step 3: Create GitHub issue**

```bash
gh issue create --title "feat: monthly savings table (income/expense/savings per month)" --body "## Description
Add a savings table storing one derived row per user per month: total income, total expense, and savings (= income − expense). Recalculated on every transaction insert/update/delete, plus a retrospective full-history pass by the monthly scheduler. Read-only GET /savings endpoint.

Spec: docs/superpowers/specs/2026-10-07-savings-monthly-table-design.md

## Acceptance criteria
- Table with unique (user_id, year, month) constraint
- recalculate() upserts/deletes correctly (empty month → no row)
- Transaction create/update/delete hooks recalc affected months (update across months recalcs both)
- RecurringJob runs recalculateAll() (full history)
- User deletion cascade removes savings before transactions
- GET /savings returns full history ascending, JWT-protected
- All tests pass"
```

- [ ] **Step 4: Push branch and open PR**

```bash
git push -u origin feat/savings-monthly-table
gh pr create --base main --head feat/savings-monthly-table --title "feat: monthly savings table" --body "Closes #<issue-number>

Spec: docs/superpowers/specs/2026-10-07-savings-monthly-table-design.md
Plan: docs/superpowers/plans/2026-10-07-savings-monthly-table.md"
```

- [ ] **Step 5: Do NOT merge — report to user for review.**
