package com.weartrack.backend.domain.closet.dto.response;

import java.util.List;

public record AiClosetPredictionResponse(
        String message,
        Integer detectedSectionCount,
        List<Integer> recommendedTemplateIds
) {
}