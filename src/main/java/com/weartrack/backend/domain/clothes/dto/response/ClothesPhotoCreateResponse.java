package com.weartrack.backend.domain.clothes.dto.response;

import com.weartrack.backend.domain.clothes.entity.AnalysisStatus;

public record ClothesPhotoCreateResponse(
        Long photoId,
        String imageUrl,
        AnalysisStatus analysisStatus,
        String predictedCategory,
        String predictedColor
) {
}