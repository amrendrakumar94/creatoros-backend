package com.creatoros.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payout details copied onto the invoice when it is raised.
 *
 * <p>Separate from {@link BankDetails} because an invoice never carries a SWIFT code and must not
 * change when the creator later edits their profile.
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceBankSnapshot {

    @Column(name = "bank_name", length = 150)
    private String bankName;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "ifsc_code", length = 20)
    private String ifscCode;

    @Column(name = "upi_id", length = 100)
    private String upiId;
}
