package com.creatoros.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Content licensing terms agreed with the brand, embedded into {@link BrandDeal}. */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRights {

    @Column(name = "exclusivity_days", nullable = false)
    @Builder.Default
    private int exclusivityDays = 0;

    @Column(name = "paid_ads_allowed", nullable = false)
    @Builder.Default
    private boolean paidAdsAllowed = false;

    @Column(name = "whitelisting_allowed", nullable = false)
    @Builder.Default
    private boolean whitelistingAllowed = false;

    @Column(length = 150)
    private String territory;
}
