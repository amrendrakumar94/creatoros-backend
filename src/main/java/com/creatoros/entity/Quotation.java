package com.creatoros.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.creatoros.enums.QuotationStatus;
import com.creatoros.enums.TdsSection;

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

@Entity
@Table(name = "quotation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long                     id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator                  creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id")
    private BrandDeal                deal;

    @Column(name = "quotation_number", nullable = false, length = 30)
    private String                   quotationNumber;

    @Column(name = "financial_year", nullable = false, length = 9)
    private String                   financialYear;

    @Column(name = "sequence_in_year", nullable = false)
    private int                      sequenceInYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private QuotationStatus          status          = QuotationStatus.DRAFT;

    @Column(name = "issue_date", nullable = false)
    private LocalDate                issueDate;

    @Column(name = "valid_until")
    private LocalDate                validUntil;

    @Embedded
    private SupplierSnapshot         supplier;

    @Embedded
    private BuyerSnapshot            buyer;

    @Column(name = "place_of_supply_state", length = 60)
    private String                   placeOfSupplyState;

    @Column(name = "place_of_supply_code", length = 2)
    private String                   placeOfSupplyCode;

    @Column(name = "inter_state", nullable = false)
    @Builder.Default
    private boolean                  interState      = false;

    @Column(name = "reverse_charge", nullable = false)
    @Builder.Default
    private boolean                  reverseCharge   = false;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal               subtotal        = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal               discountAmount  = BigDecimal.ZERO;

    @Column(name = "cgst_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal               cgstRate        = BigDecimal.ZERO;

    @Column(name = "cgst_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal               cgstAmount      = BigDecimal.ZERO;

    @Column(name = "sgst_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal               sgstRate        = BigDecimal.ZERO;

    @Column(name = "sgst_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal               sgstAmount      = BigDecimal.ZERO;

    @Column(name = "igst_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal               igstRate        = BigDecimal.ZERO;

    @Column(name = "igst_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal               igstAmount      = BigDecimal.ZERO;

    @Column(name = "total_tax", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal               totalTax        = BigDecimal.ZERO;

    @Column(name = "quotation_total", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal               quotationTotal  = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tds_section", nullable = false, length = 20)
    @Builder.Default
    private TdsSection               tdsSection      = TdsSection.NONE;

    @Column(name = "tds_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal               tdsRate         = BigDecimal.ZERO;

    @Column(name = "tds_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal               tdsAmount       = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String                   notes;

    @Column(columnDefinition = "TEXT")
    private String                   terms;

    @Column(name = "scheduled_send_at")
    private Timestamp                scheduledSendAt;

    @Column(name = "scheduled_send_email", length = 255)
    private String                   scheduledSendEmail;

    @Column(name = "last_emailed_at")
    private Timestamp                lastEmailedAt;

    @Column(name = "converted_invoice_id")
    private Long                     convertedInvoiceId;

    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, id ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<QuotationLineItem>  lineItems       = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Timestamp                createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Timestamp                updatedAt;

    public void addLineItem(QuotationLineItem item) {
        item.setQuotation(this);
        this.lineItems.add(item);
    }
}
