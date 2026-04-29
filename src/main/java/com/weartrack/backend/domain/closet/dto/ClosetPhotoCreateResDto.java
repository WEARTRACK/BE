package com.weartrack.backend.domain.closet.dto;

import java.util.List;

public record ClosetPhotoCreateResDto(
        String analysisStatus,
        Integer templateId,
        String imageUrl,
        Integer predictedSectionCount,
        List<PredictedSectionDto> predictedSections
) {
}