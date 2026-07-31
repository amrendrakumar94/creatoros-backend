package com.creatoros.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A single-use numeric code issued to a creator for signup verification or
 * password reset.
 */
@Entity
@Table(name = "otp_token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long       id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator    creator;

    @Column(nullable = false, length = 6)
    private String     code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private Instant    expiresAt;

    /** Non-null once redeemed; a consumed token can never be reused. */
    @Column(name = "consumed_at")
    private Instant    consumedAt;

    @Column(nullable = false)
    @Builder.Default
    private int        attempts = 0;

    @Column(name = "created_at", nullable = false)
    private Instant    createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
