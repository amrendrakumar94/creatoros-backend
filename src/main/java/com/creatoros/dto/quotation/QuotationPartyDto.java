package com.creatoros.dto.quotation;

public record QuotationPartyDto(

        String name,

        String legalName,

        String gstin,

        String pan,

        String email,

        String address,

        String city,

        String state,

        String stateCode,

        String pincode) {
}
