package com.creatoros.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** A sponsorship agreement between the creator and a brand. */
@Entity
@Table(name = "brand_deal")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private Creator creator;

    /** Server-assigned, unique per creator (BD-YYYY-NN). */
    @Column(name = "deal_number", nullable = false, length = 30)
    private String dealNumber;

    @Column(name = "brand_name", nullable = false, length = 200)
    private String brandName;

    @Column(name = "brand_logo", length = 20)
    private String brandLogo;

    @Column(length = 120)
    private String category;

    @Column(name = "contact_person", length = 150)
    private String contactPerson;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DealStage stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlatformType platform;

    @Column(name = "campaign_title", length = 300)
    private String campaignTitle;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sortOrder ASC, id ASC")
    @Builder.Default
    private List<DeliverableItem> deliverables = new ArrayList<>();

    @Embedded
    @Builder.Default
    private UsageRights usageRights = new UsageRights();

    @Column(name = "negotiation_notes", columnDefinition = "TEXT")
    private String negotiationNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_terms", nullable = false, length = 40)
    @Builder.Default
    private PaymentTerms paymentTerms = PaymentTerms.NET_30;

    /** Set once an invoice is raised against this deal. */
    @Column(name = "invoice_id")
    private Long invoiceId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "deal_tag", joinColumns = @JoinColumn(name = "deal_id"))
    @Column(name = "tag", nullable = false, length = 60)
    @Builder.Default
    private Set<String> tags = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Keeps both sides of the association consistent. */
    public void addDeliverable(DeliverableItem item) {
        item.setDeal(this);
        this.deliverables.add(item);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
