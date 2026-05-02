package com.weartrack.backend.domain.clothes.dto.response;

import java.time.LocalDateTime;

public record ClothesCreateResponse(
        Long clothesId,
        Long photoId,
        String imageUrl,
        String color,
        String category,
        Integer price,
        Long sectionId,
        LocalDateTime createdAt
) {
}
