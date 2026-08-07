# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Backend for CreatorOS — a business OS for independent creators in India. Spring Boot 4.0.7 on Java 21,
MySQL, Flyway, JWT auth. Serves a separate frontend; DTO shapes mirror that app's `src/types/creatorOS.ts`.

## Commands

```bash
./mvnw spring-boot:run          # run on http://localhost:8082
./mvnw clean package            # build the fat jar into target/
./mvnw compile                  # fast compile check
```

There is no `src/test` yet. Once tests exist: `./mvnw test`, single test
`./mvnw test -Dtest=ClassName#methodName`.

Requires a local MySQL `creatoros` database reachable at `localhost:3306` (root/root per
`application.properties`). Startup fails hard if the schema doesn't match the entities — see Database below.

Runtime logs go to `logs/creatoros-backend.log` (gitignored); `com.creatoros` logs at DEBUG.

## Architecture

Request flow, strictly layered — each arrow crosses a package:

```
controller → service (interface) → serviceimpl → dao (interface) → daoimpl → Hibernate Session
                                       ↕
                              DomainMapper / CreatorMapper  (entity → DTO)
```

Interfaces live in `service/` and `dao/`; implementations in `serviceimpl/` and `daoimpl/`. Add both when
introducing a new capability. Injection is constructor-based via Lombok `@RequiredArgsConstructor`.

### No Spring Data JPA — this is deliberate

`CreatorosBackendApplication` excludes `HibernateJpaAutoConfiguration` and
`DataJpaRepositoriesAutoConfiguration`. Do not add `JpaRepository`/`CrudRepository` interfaces; they will
not be wired. Persistence goes through raw Hibernate:

- `DatabaseConfig` hand-builds `mainDataSource` (Hikari), `mainSessionFactory`
  (`LocalSessionFactoryBuilder` scanning `com.creatoros.entity`), and `mainTransactionManager`. Bean names
  are `main*`-prefixed and `@Primary` so a second database can be added alongside.
- Datasource properties use a **custom `spring.main.datasource.*` prefix** read via `@Value` — not
  Boot's `spring.datasource.*`. Adding `spring.datasource.url` has no effect.
- `mainSessionFactory` calls `flywayInitializer.getIfAvailable()` to force migrations to run *before*
  Hibernate validates the schema. Don't remove that call.
- DAOs extend `HibernateDao` (`daoimpl/HibernateDao.java`) for `session()`, `persistOrMerge`,
  `removeEntity`, `executeBulk`. Queries are HQL via `session().createSelectionQuery(...)`.
- `session()` is `getCurrentSession()`, so **every DAO call needs an ambient transaction**. Transaction
  boundaries belong on `serviceimpl` methods (`@Transactional`, `readOnly = true` for reads) — never on DAOs.

### Tenant scoping is the security model

There is no row-level security. Isolation is enforced by convention, and breaking it leaks data across
creators:

- Controllers obtain the tenant with `SecurityUtils.currentCreatorId()` and pass it down as the first
  argument. Never accept a creator/owner id from the request body or path.
- DAO finders are creator-scoped by signature — `findByIdAndCreatorId`, `findByCreatorIdOrderBy...`.
  A new finder on a creator-owned entity must take `creatorId` and filter on it.
- Services resolve entities through a private `requireX(creatorId, id)` helper that throws
  `ResourceNotFoundException.of(...)`, so a cross-tenant id returns 404 rather than 403.

### Auth

Stateless JWT, no sessions. `JwtAuthenticationFilter` reads `Authorization: Bearer`, resolves the email
claim via `JwtService`, loads a `CreatorPrincipal` through `CreatorUserDetailsService`, and populates the
`SecurityContext`. `SecurityConfig` permits `/api/v1/auth/**`, `/actuator/health`, `/actuator/info`, and
all `OPTIONS`; everything else requires authentication. Passwords are BCrypt. Tokens carry the creator id
as subject plus `email` and `role` claims; secret and TTL come from `app.jwt.*` (`AppProperties`).

### Wire contract (frontend-facing — easy to break silently)

- **Enums serialize as display labels**, not names. Each enum in `enums/` pairs `@JsonValue` on
  `getLabel()` with a lenient `@JsonCreator` accepting either the label or the constant name — e.g.
  `DealStage.PAYMENT_RECEIVED` ⇄ `"Payment Received"`. New enums must follow this pattern or the frontend
  breaks. Adding a constant is a wire change.
- **Entity ids are exposed as strings.** Mappers use `idOf(Long)` → `String`. `CurrentUserResponse.id`
  is the one deliberate exception.
- `spring.jackson.default-property-inclusion=non_null` strips nulls from responses.
- Request DTOs are validated records annotated with `jakarta.validation` constraints, activated by
  `@Valid` in the controller. Nested records need `@Valid` on the field too.
- Services normalize input on write: blank strings collapse to `null` (`blankToNull`), emails lowercase,
  GSTINs uppercase.

### Errors

`GlobalExceptionHandler` (`@RestControllerAdvice`) maps everything to the `ApiError` record. Throw the
domain exceptions — `BadRequestException(message, errorCode)`, `ResourceNotFoundException.of(type, id)`,
`InvalidCredentialsException` — rather than building `ResponseEntity` error bodies in controllers.
Validation failures return a per-field map; the catch-all `Exception` handler logs and returns
`INTERNAL_ERROR`.

## Database

`spring.jpa.hibernate.ddl-auto=validate` — Hibernate never creates or alters tables. Schema changes are
Flyway-only, in `src/main/resources/db/migration/V<n>__<description>.sql`.

**Any entity change (new field, renamed column, new collection table) requires a paired migration, or the
app will not start.** `V1__init_schema.sql` is the baseline; `baseline-on-migrate=true` is set.

Conventions from `V1`: `BIGINT AUTO_INCREMENT` ids, `snake_case` columns, enums stored as
`VARCHAR` of the constant name (`@Enumerated(EnumType.STRING)` — the label mapping is JSON-only),
booleans as `BIT(1)`, money as `DECIMAL(15,2)`, timestamps as `DATETIME(6)` driven by
`@CreationTimestamp`/`@UpdateTimestamp`, `ON DELETE CASCADE` FKs to `creator`, and an index on every
`creator_id`.

Business-rule note: `GstCalculationService` computes reclaimable input tax credit as `gross × 18/118`
(GST is inclusive in the stored amount) and is applied on every expense write.

## Conventions

- Entities: Lombok `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`, with
  `@Builder.Default` on initialized collections and embeddables. Bidirectional associations get a helper
  that sets both sides (`BrandDeal.addDeliverable`).
- Collection updates on an owning entity clear and repopulate in place (`deal.getDeliverables().clear()`
  then re-add) so `orphanRemoval` fires — never reassign the collection field.
- DTOs are `record`s: `<Thing>Request` inbound, `<Thing>Dto` outbound, grouped in `dto/<feature>/`.
- Server-owned values are never taken from the client: `dealNumber` is generated as `BD-YYYY-NN` from a
  per-creator count, handles are generated at signup.
- Formatting is Eclipse-style with column-aligned field declarations and a wide (~150 col) line limit.
  Match surrounding code; the IDE profile referenced in `.idea/eclipseCodeFormatter.xml` points outside
  the repo and is not portable.

## API surface

All under `/api/v1`: `auth` (`POST /signup`, `POST /login`), `me` (`GET /`, `GET|PUT /profile`),
`deals` (full CRUD plus `PATCH /{id}/stage`), `expenses` (full CRUD). CORS origins come from
`app.cors.allowed-origins` (`CorsConfig`), defaulting to the Vite/CRA dev ports.
