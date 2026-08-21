# AGENTS.md

## Instructions

Always use the correct mvp and clean code.
For every requested change, you must first create an issue, a branch, and a PR with the modifications; do not apply changes directly. The PR will not be merged until it is approved, and only approved branches may be merged.

## Build & Run

```bash
# Build (skip tests for speed, they need a running DB)
mvn clean package -DskipTests -o

# Run (requires PostgreSQL on localhost:5432)
mvn spring-boot:run

# Run tests (requires PostgreSQL running with money_manager DB)
mvn test
```

No `./mvnw` wrapper needed — plain `mvn` works on this machine.

## Stack

- **Spring Boot 4.1.0** + **Java 25** — do NOT use springdoc 2.x, use 3.1.0+ for SB4 compatibility
- **PostgreSQL** on `localhost:5432`, database `money_manager`, user/pass `postgres/postgres`
- **JWT** via jjwt 0.12.7 (not Spring Security's built-in JWT)
- **Lombok** on all domain/DTO/service classes
- **springdoc-openapi 3.1.0** for Swagger UI at `/swagger-ui/index.html`

## Architecture: Hexagonal (Ports & Adapters)

```
domain/           → Entities, repository interfaces (ports), enums, exceptions
application/      → Services (ports), DTOs, mappers
infrastructure/   → Controllers, Spring Security, JPA repositories (adapters)
```

- `application/ports/` = service interfaces (e.g. `UserService`)
- `infrastructure/persistance/adapter/` = Spring Data adapters implementing domain repository interfaces
- `infrastructure/persistance/mapper/` = JPA entity <-> domain entity mappers
- `application/mappers/` = domain <-> DTO mappers

**Never import `infrastructure` from `domain` or `application` layers.** Domain must stay framework-free.

## Security

- Stateless JWT (no sessions). Every endpoint except login/register requires `Bearer` token.
- `SecurityConfig.java`: `/v3/api-docs/**` and `/swagger-ui/**` are `permitAll()`. When adding new public paths, add them before `.anyRequest().authenticated()`.
- Filter chain order: `RateLimiterFilter` → `JwtFilter` → `UsernamePasswordAuthenticationFilter`
- `@CrossOrigin(origins = "*")` on controllers (CORS wide open — tighten before production).

## Gotchas

- **`target/` is tracked in git** despite `.gitignore` listing it. This causes noisy diffs with `.class` files. Be aware but don't mass-remove without confirming with the team.
- **`ExceptionHandlerController` catches `Exception.class`** — returns 500 for any unhandled exception including `NoResourceFoundException`. This makes 404s appear as 500s. Narrow the catch if needed.
- **Tests are minimal** — only `contextLoads()` exists. It boots the full context (needs PostgreSQL). No unit tests yet.
- **`application.properties` has a hardcoded JWT key** — fine for dev, must be externalized for prod.
- **`ddl-auto=update`** in dev profile — schema changes apply automatically on boot. Prod uses `validate`.

## Branch Convention

- Branches: `fix/<topic>`, `feat/<topic>`
- Commits: `fix:`, `chore:`, `refactor:`, `feat:` prefixes
- PRs merged into `main` via GitHub
