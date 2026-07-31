package com.creatoros.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payout details, embedded into {@link Creator} and snapshotted onto invoices
 * later.
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetails {

    @Column(name = "bank_name", length = 150)
    private String bankName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "upi_id", length = 100)
    private String upiId;

    /** Only set for creators taking international payouts. */
    @Column(name = "swift_code", length = 20)
    private String swiftCode;
}
