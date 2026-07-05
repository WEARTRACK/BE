package com.weartrack.backend.domain.clothes.dto.request;

import jakarta.validation.constraints.Min;

public record ClothesUpdateRequest(
        String productName,
        String brandName,
        @Min(0) Integer price,
        Long closetId,
        Long sectionId
) {
}