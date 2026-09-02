# Money Manager — Architecture

This document describes the high-level architecture of the Money Manager REST API, a
Spring Boot 4.1.0 / Java 25 application that follows a **Hexagonal (Ports & Adapters)**
architecture.

---

## 1. Overview

```
┌────────────────────────── HTTP (JSON) ──────────────────────────┐
│                                                                │
│  Infrastructure   ── Controllers ── Security ── Scheduler      │
│       │                                                        │
│       │ (implements ports / adapters)                          │
│       ▼                                                        │
│  Application      ── Services (ports) ── DTOs ── Mappers       │
│       │                                                        │
│       │ (depends on domain ports)                              │
│       ▼                                                        │
│  Domain           ── Entities ── Repository ports ── Enums     │
│       │                                                        │
│       ▼                                                        │
│  PostgreSQL (via JPA / Spring Data)                            │
└────────────────────────────────────────────────────────────────┘
```

Dependency rule: **`domain` never imports `application` or `infrastructure`; the domain is
framework-free** (only `java.*` and `lombok.*`). `application` depends on `domain` ports.
`infrastructure` depends on both and provides the concrete adapters.

---

## 2. Layered breakdown

### 2.1 `domain/` — Entities, ports, enums (framework-free)

The core business model and the contracts the rest of the app depends on.

| Package | Contents |
|---|---|
| `domain/` | `User`, `Category`, `Transaction`, `Debt`, `Payment` (POJOs with Lombok) |
| `domain/*Repository.java` | **Ports** — repository interfaces (`UserRepository`, `CategoryRepository`, `TransactionRepository`, `DebtRepository`, `PaymentRepository`) |
| `domain/enums/` | `Type` (`INCOME`/`EXPENSE`), `Subtype` (`FIXED`/`VARIABLE`) with `getTypeByName`/`getSubTypeByName` factories |
| `domain/paging/` | `Page<T>`, `Pageable`, `SortDirection` — domain-agnostic paging records |
| `domain/exception/` | `NotFoundException` (checked exception) |

**Business rules in the domain:**
- `Debt.endDebt()` sets `endDate = LocalDate.now()`. A debt is "closed" when `endDate != null`.
- `Subtype.FIXED` transactions and `automaticPayment` payments trigger recurrence logic.

### 2.2 `application/` — Use cases, DTOs, mappers

Contains the service **ports**, their implementations, the DTOs that travel over HTTP, and
the mappers between DTOs and domain entities.

| Package | Contents |
|---|---|
| `application/ports/` | Service interfaces: `UserService`, `CategoryService`, `TransactionService`, `DebtService`, `PaymentService`, `RecurringService`, plus `AuthenticationPort` and `TokenService` |
| `application/services/` | Implementations (suffix `Imp`): `UserServiceImp`, `CategoryServiceImp`, `TransactionServiceImp`, `DebtServiceImp`, `PaymentServiceImp`, `RecurringServiceImp` |
| `application/dtos/` | `*RequestDTO` / `*ResponseDTO`, `TransactionFilter`, `ErrorResponseDTO` (records with Jakarta validation) |
| `application/mappers/` | Static mappers: `UserMapper`, `CategoryMapper`, `TransactionMapper`, `DebtMapper`, `PaymentMapper`, `TokenMapper` (domain ↔ DTO) |

**Notable service logic:**
- `TransactionServiceImp.createTransaction`: backfills past months for `FIXED` transactions (dedup via `existsBy...AndMonth`).
- `PaymentServiceImp.insertPayment`: validates owner, accumulates paid amount, auto-closes the debt when fully paid, and backfills automatic payments.
- `RecurringServiceImp`: monthly recurrence generation (see §4).
- `DebtServiceImp.closeDebt`: orchestration that invokes `Debt.endDebt()` domain logic.

### 2.3 `infrastructure/` — Adapters (Spring, JPA, HTTP, Security)

The concrete framework-dependent implementations.

| Package | Contents |
|---|---|
| `infrastructure/controller/` | `UserController`, `CategoryController`, `TransactionController`, `DebtController`, `PaymentController`, `HealthController`, `ExceptionHandlerController` (`@RestControllerAdvice`) |
| `infrastructure/config/` | `SecurityConfig`, `JwtFilter`, `EncodeConfig`, `TimeConfig` |
| `infrastructure/security/` | `RateLimiterFilter`, `RateLimiterService`, `SecurityUserDetails`, `SpringSecurityAuthenticationPort`, `TokenServiceImp` |
| `infrastructure/scheduler/` | `RecurringJob` (cron trigger) |
| `infrastructure/persistance/entity/` | JPA entities: `UserJpa`, `CategoryJpa`, `DebtJpa`, `PaymentJpa`, `TransactionJpa` |
| `infrastructure/persistance/adapter/` | Spring Data adapters implementing the domain repository ports (`UserRepositoryAdapter`, etc.) |
| `infrastructure/persistance/mapper/` | Static mappers JPA ↔ domain (`UserJpaMapper`, etc.) |
| `infrastructure/persistance/` | Spring Data repositories (`Postgres*`) |

---

## 3. Security model

- **Stateless JWT** (no sessions). Token generation/validation uses spring-security-oauth2-jose
  (Nimbus) with a symmetric HS256 key from `jwt.key`; issuer and audience are validated.
- **Filter chain order:** `RateLimiterFilter` → `JwtFilter` → `UsernamePasswordAuthenticationFilter`.
- `JwtFilter` reads the `Authorization: Bearer <jwt>` header, validates the token, checks the
  user still exists, and sets the `SecurityContextHolder` with the domain `User` (no roles yet).
- **Public routes** (`permitAll`): `OPTIONS`, `/health`, `POST /user`, `POST /user/login`,
  `/v3/api-docs/**`, `/swagger-ui/**`. Everything else requires authentication.
- **Rate limiting** (`RateLimiterService`, in-memory sliding window): `POST /user/login`
  (5 req/60s) and `POST /user` (10 req/60s), keyed by IP + User-Agent. Returns `429`.
- **Passwords** hashed with BCrypt; login uses `AuthenticationPort` → `AuthenticationManager`
  (`DaoAuthenticationProvider` + `UserDetailsService` adapted over the domain `User`).

### Error handling (`ExceptionHandlerController`)

| Exception | HTTP status |
|---|---|
| `BadCredentialsException` | 401 |
| `HttpMessageNotReadableException` | 400 |
| `DataIntegrityViolationException` | 409 |
| `NotFoundException` | 404 |
| `MethodArgumentTypeMismatchException` | 400 |
| `MethodArgumentNotValidException` | 400 |
| `EnumConstantNotPresentException` | 400 |
| `DateTimeParseException` | 400 |
| `Exception` (catch-all) | 500 |

---

## 4. Recurrence & scheduler

`RecurringJob` is triggered by `@Scheduled(cron = "0 5 0 1 * *")` (1st of every month at 00:05),
enabled via `@EnableScheduling` on `ManagerApplication`.

For each month it runs two processes:
- `processFixedTransactions()` — for every `FIXED` transaction across all users, creates a copy
  for the current month (**dedup** via `existsByUserCategoryNameAmountTypeSubtypeAndMonth`).
- `processAutomaticPayments()` — for automatic payments of open debts (`endDate IS NULL`),
  creates a payment for the current month (dedup via `existsByDebtAmountAndMonth`), then calls
  `closeDebtIfPaidOff()` to auto-close the debt when fully paid.

Dates are clamped with `sameDayForMonth` (e.g. day 31 maps to 28/29 in February). This
backfill/dedup logic is partially duplicated between `TransactionServiceImp`,
`PaymentServiceImp`, and `RecurringServiceImp` (see §7, known issues).

---

## 5. Persistence

- **Spring Data JPA + PostgreSQL.** `database: money_manager`, user/pass `postgres/postgres`.
- JPA entities do **not** declare `cascade`/`orphanRemoval`. Deletion of a user
  (`UserRepositoryAdapter.delete`) is a manual cascade in the application layer within one
  transaction:
  `payments → debts → transactions → categories → user`.
- Custom JPQL queries:
  - `PostgresTransactionRepository.findByFilters(Pageable)` — dynamic filter + pagination.
  - `existsBy...AndMonth` queries on payments and transactions — recurrence dedup.
- Two-layer mapping: **application mappers** (DTO ↔ domain) and **infrastructure mappers**
  (domain ↔ JPA). Adapters resolve parent JPA references (`findById`) before mapping to avoid
  transient-entity issues.

---

## 6. Controllers / API surface

| Controller | Endpoints (services) |
|---|---|
| `UserController` | `POST /user`, `POST /user/login`, `GET /user`, `PUT /user`, `DELETE /user` |
| `CategoryController` | `GET/POST/PUT/DELETE` categories |
| `TransactionController` | `GET /transaction/all` (paginated + filter), `GET/POST/PUT/DELETE` transaction |
| `DebtController` | `GET /debt/all` (paginated; each debt's payments are paged), `GET/POST/PUT/DELETE` debt |
| `PaymentController` | `GET/POST/PUT/DELETE` payment |
| `HealthController` | `GET /health` |

Dates travel as ISO strings (`yyyy-MM-dd`) in DTOs and are parsed to `LocalDate` in the mappers.
Paging is domain-agnostic (`Page<T>`/`Pageable`) and converted to Spring Data `Pageable`
inside `TransactionServiceImp`/`DebtServiceImp`.

---

## 7. Known issues / improvement opportunities

- **Domain imports Spring Data paging types.** `domain/TransactionRepository` and
  `domain/PaymentRepository` import `org.springframework.data.domain.Page`/`Pageable`. This
  slightly violates the "framework-free domain" rule.
- **Duplicated recurrence logic.** Backfill + dedup + `closeDebtIfPaidOff` logic is repeated
  across `TransactionServiceImp`, `PaymentServiceImp`, and `RecurringServiceImp`. Consider a
  shared helper/service.
- **`target/` is tracked in git** despite `.gitignore`. Build artifacts cause noisy diffs.
- **`ExceptionHandlerController` catches `Exception.class`** → any unhandled exception
  (including `NoResourceFoundException`) returns 500, so 404s can appear as 500s. Narrow if needed.
- **Hardcoded JWT key** in `application.properties` — must be externalized for production.
- **`PaymentJpaMapper.toDomain()`** builds only a partial `Debt` (id only) to avoid lazy-load/cycles.
- **No roles/authorities yet** — `SecurityUserDetails.getAuthorities()` returns an empty list.
