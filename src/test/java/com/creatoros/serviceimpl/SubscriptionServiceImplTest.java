package com.creatoros.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.creatoros.dao.CreatorSubscriptionDao;
import com.creatoros.entity.Creator;
import com.creatoros.entity.CreatorSubscription;
import com.creatoros.enums.CreatorStatus;
import com.creatoros.enums.Role;
import com.creatoros.enums.SubscriptionPlan;
import com.creatoros.exception.BadRequestException;
import com.creatoros.exception.ResourceNotFoundException;
import com.creatoros.security.CreatorPrincipal;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    private static final Long OWNER_A = 1L;
    private static final Long MEMBER  = 2L;

    @Mock
    private CreatorSubscriptionDao subscriptionDao;

    // Real, not mocked - the trial-expiry derivation lives here, and these tests exercise it.
    private final DomainMapper domainMapper = new DomainMapper();

    private SubscriptionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionServiceImpl(subscriptionDao, domainMapper);
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("blocks a non-owner, non-admin caller from subscribing")
    void nonOwnerNonAdminBlockedFromSubscribing() {
        authenticateAsMember(MEMBER, OWNER_A);

        assertThatThrownBy(() -> service.subscribe(OWNER_A)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("OWNER_REQUIRED"));
    }

    @Test
    @DisplayName("blocks a non-owner, non-admin caller from cancelling")
    void nonOwnerNonAdminBlockedFromCancelling() {
        authenticateAsMember(MEMBER, OWNER_A);

        assertThatThrownBy(() -> service.cancel(OWNER_A)).isInstanceOf(BadRequestException.class)
                .satisfies(ex -> assertThat(((BadRequestException) ex).getErrorCode()).isEqualTo("OWNER_REQUIRED"));
    }

    @Test
    @DisplayName("allows the workspace owner to subscribe")
    void ownerCanSubscribe() {
        authenticateAsOwner(OWNER_A);
        CreatorSubscription subscription = trialSubscription(OWNER_A, futureTimestamp());
        when(subscriptionDao.findByCreatorId(OWNER_A)).thenReturn(Optional.of(subscription));

        service.subscribe(OWNER_A);

        assertThat(subscription.getPlan()).isEqualTo(SubscriptionPlan.SUBSCRIPTION);
        assertThat(subscription.getSubscribedAt()).isNotNull();
        verify(subscriptionDao, times(1)).save(subscription);
    }

    @Test
    @DisplayName("repeated subscribe is idempotent - no save when already subscribed")
    void repeatedSubscribeIsNoOp() {
        authenticateAsOwner(OWNER_A);
        CreatorSubscription subscription = subscribedSubscription(OWNER_A);
        when(subscriptionDao.findByCreatorId(OWNER_A)).thenReturn(Optional.of(subscription));

        service.subscribe(OWNER_A);

        verify(subscriptionDao, never()).save(any());
    }

    @Test
    @DisplayName("repeated cancel is idempotent - no save when already on trial")
    void repeatedCancelIsNoOp() {
        authenticateAsOwner(OWNER_A);
        CreatorSubscription subscription = trialSubscription(OWNER_A, futureTimestamp());
        when(subscriptionDao.findByCreatorId(OWNER_A)).thenReturn(Optional.of(subscription));

        service.cancel(OWNER_A);

        verify(subscriptionDao, never()).save(any());
    }

    @Test
    @DisplayName("cancel reverts a subscription back to trial")
    void cancelRevertsToTrial() {
        authenticateAsOwner(OWNER_A);
        CreatorSubscription subscription = subscribedSubscription(OWNER_A);
        when(subscriptionDao.findByCreatorId(OWNER_A)).thenReturn(Optional.of(subscription));

        service.cancel(OWNER_A);

        assertThat(subscription.getPlan()).isEqualTo(SubscriptionPlan.TRIAL);
        verify(subscriptionDao, times(1)).save(subscription);
    }

    @Test
    @DisplayName("allows a platform admin to subscribe even when they don't own the workspace")
    void platformAdminAllowedEvenWhenNotOwner() {
        authenticateAs(MEMBER, OWNER_A, Role.ADMIN);
        when(subscriptionDao.findByCreatorId(OWNER_A)).thenReturn(Optional.of(trialSubscription(OWNER_A, futureTimestamp())));

        assertThatCode(() -> service.subscribe(OWNER_A)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("createDefaultSubscription is a no-op when a row already exists")
    void createDefaultSubscriptionNoOpsWhenRowExists() {
        when(subscriptionDao.findByCreatorId(OWNER_A)).thenReturn(Optional.of(trialSubscription(OWNER_A, futureTimestamp())));

        service.createDefaultSubscription(OWNER_A);

        verify(subscriptionDao, never()).save(any());
    }

    @Test
    @DisplayName("createDefaultSubscription creates a 30-day trial row when none exists")
    void createDefaultSubscriptionCreatesTrialRow() {
        when(subscriptionDao.findByCreatorId(OWNER_A)).thenReturn(Optional.empty());

        service.createDefaultSubscription(OWNER_A);

        verify(subscriptionDao, times(1)).save(any(CreatorSubscription.class));
    }

    @Test
    @DisplayName("getCurrent throws when no subscription row exists")
    void getCurrentThrowsWhenMissing() {
        when(subscriptionDao.findByCreatorId(OWNER_A)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrent(OWNER_A)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("an active trial has full access and is not expired")
    void activeTrialHasFullAccess() {
        when(subscriptionDao.findByCreatorId(OWNER_A)).thenReturn(Optional.of(trialSubscription(OWNER_A, futureTimestamp())));

        var dto = service.getCurrent(OWNER_A);

        assertThat(dto.hasFullAccess()).isTrue();
        assertThat(dto.trialExpired()).isFalse();
    }

    @Test
    @DisplayName("an expired trial has no full access and is marked expired")
    void expiredTrialHasNoFullAccess() {
        when(subscriptionDao.findByCreatorId(OWNER_A)).thenReturn(Optional.of(trialSubscription(OWNER_A, pastTimestamp())));

        var dto = service.getCurrent(OWNER_A);

        assertThat(dto.hasFullAccess()).isFalse();
        assertThat(dto.trialExpired()).isTrue();
    }

    @Test
    @DisplayName("a subscription has full access regardless of trialEndsAt")
    void subscriptionHasFullAccessRegardlessOfTrialEndsAt() {
        when(subscriptionDao.findByCreatorId(OWNER_A)).thenReturn(Optional.of(subscribedSubscription(OWNER_A)));

        var dto = service.getCurrent(OWNER_A);

        assertThat(dto.hasFullAccess()).isTrue();
        assertThat(dto.trialExpired()).isFalse();
    }

    private static Timestamp futureTimestamp() {
        return new Timestamp(System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000);
    }

    private static Timestamp pastTimestamp() {
        return new Timestamp(System.currentTimeMillis() - 10L * 24 * 60 * 60 * 1000);
    }

    private static CreatorSubscription trialSubscription(Long creatorId, Timestamp trialEndsAt) {
        return CreatorSubscription.builder().id(10L).creatorId(creatorId).plan(SubscriptionPlan.TRIAL).trialEndsAt(trialEndsAt).build();
    }

    private static CreatorSubscription subscribedSubscription(Long creatorId) {
        return CreatorSubscription.builder().id(10L).creatorId(creatorId).plan(SubscriptionPlan.SUBSCRIPTION).trialEndsAt(pastTimestamp())
                .subscribedAt(new Timestamp(System.currentTimeMillis())).build();
    }

    private void authenticateAsOwner(Long creatorId) {
        authenticateAs(creatorId, creatorId, Role.CREATOR);
    }

    private void authenticateAsMember(Long memberCreatorId, Long tenantId) {
        authenticateAs(memberCreatorId, tenantId, Role.CREATOR);
    }

    private void authenticateAs(Long creatorId, Long tenantId, Role platformRole) {
        Creator creator = creator(creatorId, "user" + creatorId + "@example.com");
        creator.setRole(platformRole);
        CreatorPrincipal principal = new CreatorPrincipal(creator, tenantId, Set.of());
        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static Creator creator(Long id, String email) {
        return Creator.builder().id(id).email(email).passwordHash("hash").status(CreatorStatus.ACTIVE).role(Role.CREATOR).name("Test")
                .handle("test-" + id).build();
    }
}
