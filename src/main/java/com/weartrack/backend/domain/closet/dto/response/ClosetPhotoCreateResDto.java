package com.weartrack.backend.domain.closet.dto.response;

import java.util.List;

public record ClosetPhotoCreateResDto(
        String analysisStatus,
        String message,
        String imageUrl,
        Integer detectedSectionCount,
        List<RecommendedTemplateDto> recommendedTemplates
) {
}