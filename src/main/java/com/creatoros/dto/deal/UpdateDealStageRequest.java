package com.creatoros.dto.deal;

import com.creatoros.entity.DealStage;
import jakarta.validation.constraints.NotNull;

public record UpdateDealStageRequest(

        @NotNull(message = "Stage is required")

        DealStage stage) {
}
