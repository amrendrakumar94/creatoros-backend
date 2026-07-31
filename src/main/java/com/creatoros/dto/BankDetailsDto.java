package com.creatoros.dto;

/**
 * Mirrors {@code CreatorProfile.bankDetails} in the frontend's
 * src/types/creatorOS.ts.
 */
public record BankDetailsDto(

        String bankName,

        String accountNumber,

        String ifscCode,

        String upiId,

        String swiftCode) {
}
