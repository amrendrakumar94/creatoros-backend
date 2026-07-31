package com.creatoros.dto.deal;

import com.creatoros.entity.DeliverableStatus;
import com.creatoros.entity.DeliverableType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Mirrors {@code DeliverableItem} in src/types/creatorOS.ts.
 *
 * <p>{@code id} is a string because the frontend types model every id as a string; it is null on
 * create and populated by the server thereafter.
 */
public record DeliverableItemDto(
        String id,

        @NotNull(message = "Deliverable type is required")
        DeliverableType type,

        @Size(max = 300)
        String title,

        LocalDate dueDate,

        DeliverableStatus status,

        @Size(max = 500)
        String link) {
}
