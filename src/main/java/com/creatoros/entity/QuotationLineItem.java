package com.creatoros.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "quotation_line_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long       id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation  quotation;

    @Column(nullable = false, length = 500)
    private String     description;

    @Column(name = "sac_code", length = 10)
    private String     sacCode;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal quantity      = BigDecimal.ONE;

    @Column(length = 20)
    private String     unit;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal rate;

    @Column(name = "gst_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gstRate       = new BigDecimal("18.00");

    @Column(name = "taxable_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal taxableAmount;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int        sortOrder     = 0;
}
