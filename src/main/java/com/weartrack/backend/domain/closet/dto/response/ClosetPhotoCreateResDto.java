package com.weartrack.backend.domain.closet.dto.response;

import com.weartrack.backend.domain.closet.dto.PredictedSectionDto;

import java.util.List;

public record ClosetPhotoCreateResDto(
        String analysisStatus,
        Integer templateId,
        String imageUrl,
        Integer predictedSectionCount,
        List<PredictedSectionDto> predictedSections
) {
}