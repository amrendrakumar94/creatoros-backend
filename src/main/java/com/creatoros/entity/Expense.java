package com.creatoros.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
import com.creatoros.enums.ExpenseCategory;
import com.creatoros.enums.PaymentMethod;

@Entity
@Table(name = "expense")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long            id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator         creator;

    @Column(nullable = false, length = 300)
    private String          title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal      amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate       expenseDate;

    @Column(length = 200)
    private String          vendor;

    @Column(length = 15)
    private String          gstin;

    @Column(name = "has_gst_invoice", nullable = false)
    @Builder.Default
    private boolean         hasGstInvoice      = false;

    @Column(name = "gst_claimable_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal      gstClaimableAmount = BigDecimal.ZERO;

    @Column(name = "receipt_url", length = 500)
    private String          receiptUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod   paymentMethod;

    @Column(columnDefinition = "TEXT")
    private String          notes;

    @Column(name = "tax_deductible", nullable = false)
    @Builder.Default
    private boolean         taxDeductible      = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Timestamp       createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Timestamp       updatedAt;

}
