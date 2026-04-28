package com.weartrack.backend.domain.clothes.dto;

import java.time.LocalDateTime;

public record ClothesCreateResponse(
        Long clothesId,
        Long photoId,
        String imageUrl,
        String color,
        String category,
        Integer price,
        Long setionId,
        LocalDateTime createdAt
) {
}
