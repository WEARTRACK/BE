package com.weartrack.backend.domain.clothes.dto.response;

import com.weartrack.backend.domain.closet.entity.ClosetSection;
import com.weartrack.backend.domain.clothes.entity.Clothes;

public record ClothesDetailResDto(
        Long clothesId,
        String imageUrl,
        String productName,
        String color,
        String category,
        Integer price,
        java.time.LocalDate purchaseDate,
        String storageLocation,
        Long sectionId,
        String sectionName
) {
    public static ClothesDetailResDto from(Clothes clothes, ClosetSection section) {
        return new ClothesDetailResDto(
                clothes.getId(),
                clothes.getImageUrl(),
                clothes.getProductName(),
                clothes.getColor(),
                clothes.getCategory(),
                clothes.getPrice(),
                clothes.getPurchaseDate(),
                clothes.getStorageLocation(),
                section.getSectionId(),
                section.getSectionName()
        );
    }
}
