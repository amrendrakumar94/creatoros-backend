package com.creatoros.entity;

import java.io.Serializable;

import com.creatoros.enums.DocumentType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DocumentCounterId implements Serializable {

    @Column(name = "creator_id", nullable = false)
    private Long         creatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 20)
    private DocumentType docType;

    @Column(name = "financial_year", nullable = false, length = 9)
    private String       financialYear;
}
