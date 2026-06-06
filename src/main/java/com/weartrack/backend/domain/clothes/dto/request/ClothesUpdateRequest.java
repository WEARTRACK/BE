package com.weartrack.backend.domain.clothes.dto.request;

import jakarta.validation.constraints.Min;

public record ClothesUpdateRequest(
       @Min(0) Integer price,
        Long sectionId
) {
}
