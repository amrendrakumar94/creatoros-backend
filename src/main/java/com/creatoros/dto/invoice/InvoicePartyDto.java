package com.creatoros.dto.invoice;

public record InvoicePartyDto(

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
