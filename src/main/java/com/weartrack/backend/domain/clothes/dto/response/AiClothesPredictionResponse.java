package com.weartrack.backend.domain.clothes.dto.response;

import com.weartrack.backend.domain.clothes.dto.ResultDto;

import java.util.List;

public record AiClothesPredictionResponse(
        String message,
        List<ResultDto> results
) {
}
