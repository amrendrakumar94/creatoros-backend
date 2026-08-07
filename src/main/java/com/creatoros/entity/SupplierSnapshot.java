package com.creatoros.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierSnapshot {

    @Column(name = "supplier_legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(name = "supplier_gstin", length = 15)
    private String gstin;

    @Column(name = "supplier_pan", length = 10)
    private String pan;

    @Column(name = "supplier_address", length = 500)
    private String address;

    @Column(name = "supplier_city", length = 120)
    private String city;

    @Column(name = "supplier_state", length = 60)
    private String state;

    @Column(name = "supplier_state_code", length = 2)
    private String stateCode;

    @Column(name = "supplier_pincode", length = 10)
    private String pincode;
}
