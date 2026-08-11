# Subscription Modal Implementation Plan

Single subscription tier (PRO) for creators. Follows existing layered architecture:
`controller → service (interface) → serviceimpl → dao (interface) → daoimpl → Hibernate`.

All tenant scoping via `creatorId` from `SecurityUtils.currentTenantId()`. No Spring Data JPA.
Every entity change requires a paired Flyway migration.

---

## 1. Database Migration — `V5__subscription.sql`

Path: `src/main/resources/db/migration/V5__subscription.sql`

```sql
CREATE TABLE subscription (
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    creator_id         BIGINT         NOT NULL,
    plan               VARCHAR(20)    NOT NULL,
    status             VARCHAR(20)    NOT NULL,
    started_at         TIMESTAMP(6)   NOT NULL,
    expires_at         TIMESTAMP(6)   NULL,
    cancelled_at       TIMESTAMP(6)   NULL,
    payment_reference  VARCHAR(200)   NULL,
    created_at         TIMESTAMP(6)   NOT NULL,
    updated_at         TIMESTAMP(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_subscription_creator UNIQUE (creator_id),
    CONSTRAINT fk_subscription_creator FOREIGN KEY (creator_id) REFERENCES creator (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_subscription_creator ON subscription (creator_id);
```

Notes:
- `UNIQUE (creator_id)` — one subscription row per creator (upsert in service).
- Enums as `VARCHAR` of constant name (Hibernate `@Enumerated(EnumType.STRING)`).
- Money fields omitted for now; if collecting payment, add `amount DECIMAL(15,2)` and `currency VARCHAR(3)`.

---

## 2. Enums

### `enums/SubscriptionPlan.java`
```java
public enum SubscriptionPlan {
    FREE,
    PRO;   // the single paid tier for now
    // @JsonValue / @JsonCreator label pattern: "Free", "Pro"
}
```

### `enums/SubscriptionStatus.java`
```java
public enum SubscriptionStatus {
    ACTIVE,
    EXPIRED,
    CANCELLED,
    PAST_DUE;
    // label pattern: "Active", "Expired", "Cancelled", "Past Due"
}
```

Both follow the existing `@JsonValue`/`@JsonCreator` label convention so the frontend sees display strings.

---

## 3. Entity — `entity/Subscription.java`

```java
@Entity @Table(name = "subscription")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Subscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "started_at", nullable = false)
    private Timestamp startedAt;

    @Column(name = "expires_at")
    private Timestamp expiresAt;

    @Column(name = "cancelled_at")
    private Timestamp cancelledAt;

    @Column(name = "payment_reference", length = 200)
    private String paymentReference;

    @CreationTimestamp @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @UpdateTimestamp @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
}
```

---

## 4. DAO

### `dao/SubscriptionDao.java`
```java
public interface SubscriptionDao {
    Subscription save(Subscription subscription);
    Optional<Subscription> findByCreatorId(Long creatorId);
    Optional<Subscription> findByIdAndCreatorId(Long id, Long creatorId);
}
```

### `daoimpl/SubscriptionDaoImpl.java`
- Extends `HibernateDao`.
- `save` → `persistOrMerge(sub, sub.getId())`.
- `findByCreatorId` → HQL `from Subscription s where s.creator.id = :creatorId`.
- `findByIdAndCreatorId` → tenant-scoped lookup.

---

## 5. DTOs — `dto/subscription/`

### `SubscriptionRequest.java`
```java
public record SubscriptionRequest(
    @NotNull SubscriptionPlan plan,
    @Size(max = 200) String paymentReference
) {}
```

### `SubscriptionDto.java`
```java
public record SubscriptionDto(
    String id,
    SubscriptionPlan plan,
    SubscriptionStatus status,
    Timestamp startedAt,
    Timestamp expiresAt,
    Timestamp cancelledAt,
    String paymentReference,
    boolean active,          // computed: status == ACTIVE && (expiresAt == null || expiresAt > now)
    long daysRemaining       // computed: days until expiresAt, or -1 if none
) {}
```

Id exposed as string via `idOf(Long)` per wire contract. Nulls stripped by Jackson.

---

## 6. Mapper — add to `serviceimpl/DomainMapper.java`

```java
public SubscriptionDto toSubscriptionDto(Subscription s) {
    boolean active = s.getStatus() == SubscriptionStatus.ACTIVE
        && (s.getExpiresAt() == null || s.getExpiresAt().after(now()));
    long daysRemaining = s.getExpiresAt() == null ? -1
        : ChronoUnit.DAYS.between(now().toInstant(), s.getExpiresAt().toInstant());
    return new SubscriptionDto(
        idOf(s.getId()), s.getPlan(), s.getStatus(),
        s.getStartedAt(), s.getExpiresAt(), s.getCancelledAt(),
        s.getPaymentReference(), active, daysRemaining
    );
}
```

---

## 7. Service

### `service/SubscriptionService.java`
```java
public interface SubscriptionService {
    SubscriptionDto getCurrent(Long creatorId);
    SubscriptionDto subscribe(Long creatorId, SubscriptionRequest request);
    SubscriptionDto cancel(Long creatorId);
}
```

### `serviceimpl/SubscriptionServiceImpl.java`
- `@Service @RequiredArgsConstructor`.
- Deps: `SubscriptionDao`, `CreatorDao`, `DomainMapper`.
- `getCurrent`: `findByCreatorId` → 404 if none (or return a FREE default; decide).
- `subscribe`:
  - Resolve creator via `creatorDao.findById` (else 404).
  - If existing subscription exists → update plan, reset `expiresAt` to `now + durationFor(plan)`, set status ACTIVE, clear cancelledAt.
  - Else create new with `startedAt = now`, `expiresAt = now + duration`, status ACTIVE.
  - `paymentReference` normalized via `blankToNull`.
- `cancel`: require subscription, set status CANCELLED, `cancelledAt = now`. Keep `expiresAt` so access continues until expiry.
- `@Transactional` on writes, `readOnly = true` on reads.
- Permissions: gate all three with `SecurityUtils.requireAny(PermissionKey.MANAGE_SUBSCRIPTION)`.

---

## 8. Permission Key — `enums/PermissionKey.java`

Add:
```java
MANAGE_SUBSCRIPTION("Manage Subscription"),
```

Assign to creator `Role.OWNER` in the team/permissions seed (whatever seeds existing permissions).

---

## 9. Controller — `controller/SubscriptionController.java`

```java
@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<SubscriptionDto> current() {
        return ResponseEntity.ok(subscriptionService.getCurrent(SecurityUtils.currentTenantId()));
    }

    @PostMapping
    public ResponseEntity<SubscriptionDto> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.subscribe(SecurityUtils.currentTenantId(), request));
    }

    @DeleteMapping
    public ResponseEntity<SubscriptionDto> cancel() {
        return ResponseEntity.ok(subscriptionService.cancel(SecurityUtils.currentTenantId()));
    }
}
```

`SecurityConfig` already requires auth for everything outside `/auth/**` and health — no change needed.

---

## 10. Config — `AppProperties.java`

Add nested `Subscription` config:
```java
private Subscription subscription = new Subscription();

@Getter @Setter
public static class Subscription {
    private int proDurationDays = 30;
}
```

Service reads `appProperties.getSubscription().getProDurationDays()` when computing `expiresAt`.

---

## 11. Tests

### `SubscriptionServiceImplTest.java`
- subscribe creates row with ACTIVE status and correct `expiresAt`.
- subscribe again (same creator) upserts — no duplicate row.
- cancel sets CANCELLED and `cancelledAt`, leaves `expiresAt`.
- getCurrent returns 404 when none (or FREE default — match decision above).
- cross-creator access returns 404 (tenant scoping).

### `SubscriptionDaoImplTest` (optional)
- `findByCreatorId` returns the row; `findByIdAndCreatorId` rejects other creator.

---

## 12. Wire Contract Notes

- `SubscriptionPlan` and `SubscriptionStatus` serialize as labels ("Pro", "Active") — frontend uses those strings.
- `id` is a string; only `SubscriptionDto` carries it.
- Adding `FREE` vs `PRO`, or a new status later, is a wire change — bump frontend types in lockstep.
- Null fields stripped from JSON (`non_null` inclusion).

---

## 13. Build & Verify

```bash
export JAVA_HOME=/Users/Amrendra/Library/Java/JavaVirtualMachines/corretto-21.0.12/Contents/Home
./mvnw clean test
./mvnw spring-boot:run
```

Requires local MySQL `creatoros` at `localhost:3306` (root/root). Flyway runs on startup before Hibernate `validate`.

---

## Open Decisions

1. **FREE default on `getCurrent`**: return a synthetic FREE DTO when no row exists, or 404? Recommend synthetic FREE (frontend always has a shape).
2. **Payment integration**: stub `paymentReference` as a free-text field, or block `subscribe` until a real gateway? Recommend stub for now.
3. **Expiry enforcement**: should an expired subscription downgrade features automatically, or just report `active=false`? Recommend report-only for v1; enforcement comes in a later ticket.

---

## File Checklist

- [ ] `src/main/resources/db/migration/V5__subscription.sql`
- [ ] `src/main/java/com/creatoros/enums/SubscriptionPlan.java`
- [ ] `src/main/java/com/creatoros/enums/SubscriptionStatus.java`
- [ ] `src/main/java/com/creatoros/entity/Subscription.java`
- [ ] `src/main/java/com/creatoros/dao/SubscriptionDao.java`
- [ ] `src/main/java/com/creatoros/daoimpl/SubscriptionDaoImpl.java`
- [ ] `src/main/java/com/creatoros/dto/subscription/SubscriptionRequest.java`
- [ ] `src/main/java/com/creatoros/dto/subscription/SubscriptionDto.java`
- [ ] `src/main/java/com/creatoros/service/SubscriptionService.java`
- [ ] `src/main/java/com/creatoros/serviceimpl/SubscriptionServiceImpl.java`
- [ ] `src/main/java/com/creatoros/controller/SubscriptionController.java`
- [ ] `src/main/java/com/creatoros/enums/PermissionKey.java` (add constant)
- [ ] `src/main/java/com/creatoros/config/AppProperties.java` (add Subscription config)
- [ ] `src/main/java/com/creatoros/serviceimpl/DomainMapper.java` (add `toSubscriptionDto`)
- [ ] `src/test/java/com/creatoros/serviceimpl/SubscriptionServiceImplTest.java`
