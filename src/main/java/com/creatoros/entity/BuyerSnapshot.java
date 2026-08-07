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
public class BuyerSnapshot {

    @Column(name = "buyer_name", nullable = false, length = 200)
    private String name;

    @Column(name = "buyer_legal_name", length = 200)
    private String legalName;

    @Column(name = "buyer_gstin", length = 15)
    private String gstin;

    @Column(name = "buyer_email", length = 255)
    private String email;

    @Column(name = "buyer_address", length = 500)
    private String address;

    @Column(name = "buyer_city", length = 120)
    private String city;

    @Column(name = "buyer_state", length = 60)
    private String state;

    @Column(name = "buyer_state_code", length = 2)
    private String stateCode;

    @Column(name = "buyer_pincode", length = 10)
    private String pincode;
}
