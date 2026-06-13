package com.weartrack.backend.domain.clothes.dto.response;

import java.time.LocalDateTime;
import java.time.LocalDate;

public record ClothesCreateResponse(
        Long clothesId,
        Long photoId,
        String imageUrl,
        String productName,
        String color,
        String category,
        Integer price,
        LocalDate purchaseDate,
        String storageLocation,
        Long sectionId,
        LocalDateTime createdAt
) {
}
