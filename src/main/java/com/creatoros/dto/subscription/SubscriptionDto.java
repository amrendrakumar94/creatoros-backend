package com.creatoros.dto.subscription;

import java.sql.Timestamp;

import com.creatoros.enums.SubscriptionPlan;

public record SubscriptionDto(

        String id,

        String creatorId,

        SubscriptionPlan plan,

        Timestamp trialEndsAt,

        Timestamp subscribedAt,

        boolean hasFullAccess,

        boolean trialExpired,

        Timestamp createdAt,

        Timestamp updatedAt,

        /** A pending Razorpay checkout link - null once payment is confirmed and the plan is active. */
        String checkoutUrl) {
}
