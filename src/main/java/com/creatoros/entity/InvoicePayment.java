package com.creatoros.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.creatoros.enums.SettlementMethod;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoice_payment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long             id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator          creator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice          invoice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal       amount;

    @Column(name = "received_on", nullable = false)
    private LocalDate        receivedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SettlementMethod method;

    @Column(length = 120)
    private String           reference;

    /** Set only when {@code method == RAZORPAY}; unique so a re-delivered webhook can't double-record. */
    @Column(name = "razorpay_payment_id", length = 64)
    private String           razorpayPaymentId;

    @Column(name = "tds_withheld", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal       tdsWithheld = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String           notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Timestamp        createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Timestamp        updatedAt;
}
