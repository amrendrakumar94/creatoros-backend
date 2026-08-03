package com.creatoros.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.creatoros.enums.InvoiceStatus;

/**
 * An Indian B2B tax invoice.
 *
 * <p>
 * Creator identity and payout details are snapshotted onto the row at issue
 * time rather than joined from {@link Creator}: a tax document must keep
 * showing the details that were valid when it was raised, even if the profile
 * changes later.
 */
@Entity
@Table(name = "invoice")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long                id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator             creator;

    /** Server-assigned, unique per creator (COS-YYYY-NNN). */
    @Column(name = "invoice_number", nullable = false, length = 30)
    private String              invoiceNumber;

    @Column(name = "brand_name", nullable = false, length = 200)
    private String              brandName;

    @Column(name = "brand_gstin", length = 15)
    private String              brandGstin;

    @Column(name = "brand_address", length = 500)
    private String              brandAddress;

    @Column(name = "creator_name", length = 200)
    private String              creatorName;

    @Column(name = "creator_gstin", length = 15)
    private String              creatorGstin;

    @Column(name = "creator_pan", length = 10)
    private String              creatorPan;

    @Embedded
    @Builder.Default
    private InvoiceBankSnapshot bankDetails       = new InvoiceBankSnapshot();

    @Column(name = "issue_date", nullable = false)
    private LocalDate           issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate           dueDate;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC, id ASC")
    @Builder.Default
    private List<InvoiceItem>   items             = new ArrayList<>();

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal          subtotal;

    @Column(name = "is_interstate", nullable = false)
    private boolean             interstate;

    @Column(name = "cgst_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal          cgstAmount;

    @Column(name = "sgst_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal          sgstAmount;

    @Column(name = "igst_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal          igstAmount;

    @Column(name = "total_gst", nullable = false, precision = 15, scale = 2)
    private BigDecimal          totalGst;

    @Column(name = "tds_deducted", nullable = false, precision = 15, scale = 2)
    private BigDecimal          tdsDeducted;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal          totalAmount;

    @Column(name = "net_receivable", nullable = false, precision = 15, scale = 2)
    private BigDecimal          netReceivable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus       status            = InvoiceStatus.DRAFT;

    @Column(name = "deal_id")
    private Long                dealId;

    @Column(name = "paid_date")
    private LocalDate           paidDate;

    @Column(name = "reminder_sent_count", nullable = false)
    @Builder.Default
    private int                 reminderSentCount = 0;

    @Column(name = "last_reminder_date")
    private LocalDate           lastReminderDate;

    @Column(name = "expected_settlement_date")
    private LocalDate           expectedSettlementDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant             createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant             updatedAt;

    public void addItem(InvoiceItem item) {
        item.setInvoice(this);
        this.items.add(item);
    }

}
