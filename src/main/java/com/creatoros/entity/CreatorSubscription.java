package com.creatoros.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.creatoros.enums.SubscriptionPlan;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "creator_subscription")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatorSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long             id;

    @Column(name = "creator_id", nullable = false, unique = true)
    private Long             creatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubscriptionPlan plan = SubscriptionPlan.TRIAL;

    @Column(name = "trial_ends_at")
    private Timestamp        trialEndsAt;

    @Column(name = "subscribed_at")
    private Timestamp        subscribedAt;

    /** A pending checkout - cleared once the webhook confirms payment and activates the plan. */
    @Column(name = "razorpay_payment_link_id", length = 64)
    private String           razorpayPaymentLinkId;

    @Column(name = "razorpay_payment_link_url", length = 255)
    private String           razorpayPaymentLinkUrl;

    /** The payment that activated the current subscription - guards a re-delivered webhook from re-activating. */
    @Column(name = "razorpay_payment_id", length = 64)
    private String           razorpayPaymentId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Timestamp        createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Timestamp        updatedAt;
}
