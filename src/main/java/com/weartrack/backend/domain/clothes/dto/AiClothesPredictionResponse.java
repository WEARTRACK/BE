package com.weartrack.backend.domain.clothes.dto;

import java.util.List;

public record AiClothesPredictionResponse(
        String message,
        List<ResultDto> results
) {
}
