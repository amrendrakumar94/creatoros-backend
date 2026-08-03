package com.creatoros.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import com.creatoros.enums.DeliverableStatus;
import com.creatoros.enums.DeliverableType;

@Entity
@Table(name = "deliverable_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long              id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deal_id", nullable = false)
    private BrandDeal         deal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DeliverableType   type;

    @Column(length = 300)
    private String            title;

    @Column(name = "due_date")
    private LocalDate         dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    @Builder.Default
    private DeliverableStatus status    = DeliverableStatus.PENDING;

    @Column(length = 500)
    private String            link;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int               sortOrder = 0;
}
