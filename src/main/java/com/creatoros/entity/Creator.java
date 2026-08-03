package com.creatoros.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.creatoros.enums.CreatorStatus;
import com.creatoros.enums.CreatorType;
import com.creatoros.enums.PlatformType;
import com.creatoros.enums.Role;

@Entity
@Table(name = "creator")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Creator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long              id;

    @Column(nullable = false, unique = true, length = 255)
    private String            email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String            passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreatorStatus     status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role              role;

    @Column(nullable = false, length = 150)
    private String            name;

    @Column(nullable = false, unique = true, length = 100)
    private String            handle;

    @Column(length = 500)
    private String            avatar;

    @Column(length = 30)
    private String            phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "creator_type", length = 40)
    private CreatorType       creatorType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "creator_platform", joinColumns = @JoinColumn(name = "creator_id"))
    @Column(name = "platform", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<PlatformType> platforms              = new LinkedHashSet<>();

    @Column(name = "is_gst_registered", nullable = false)
    @Builder.Default
    private boolean           gstRegistered          = false;

    @Column(length = 15)
    private String            gstin;

    @Column(length = 10)
    private String            pan;

    @Column(name = "trade_name", length = 200)
    private String            tradeName;

    @Column(length = 500)
    private String            address;

    @Column(length = 120)
    private String            city;

    @Column(length = 10)
    private String            pincode;

    @Column(name = "monthly_revenue_estimate", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal        monthlyRevenueEstimate = BigDecimal.ZERO;

    @Column(name = "team_size", length = 50)
    private String            teamSize;

    @Embedded
    @Builder.Default
    private BankDetails       bankDetails            = new BankDetails();

    @Column(name = "onboarding_completed", nullable = false)
    @Builder.Default
    private boolean           onboardingCompleted    = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant           createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant           updatedAt;

}
