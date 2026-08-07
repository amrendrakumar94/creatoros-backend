package com.creatoros.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_counter")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCounter {

    @EmbeddedId
    private DocumentCounterId id;

    @Column(name = "last_sequence", nullable = false)
    private int               lastSequence;
}
