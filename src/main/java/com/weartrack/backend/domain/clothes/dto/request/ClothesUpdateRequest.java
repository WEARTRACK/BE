package com.weartrack.backend.domain.clothes.dto.request;

import jakarta.validation.constraints.Min;

public record ClothesUpdateRequest(
        String color,
        String category,
       @Min(0) Integer price,
        Long sectionId
) {
}
