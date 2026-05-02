package com.weartrack.backend.domain.clothes.dto.response;

import com.weartrack.backend.domain.closet.entity.ClosetSection;
import com.weartrack.backend.domain.clothes.entity.Clothes;

import java.util.List;

public record ClothesListResDto(
        String sectionName,
        Integer totalCount,
        List<ClothesItem> clothes
) {
    public record ClothesItem(
        Long clothesId,
        String imageUrl,
        String color,
        String category
) {
    public static ClothesItem from(Clothes clothes) {
        return new ClothesItem(
                clothes.getId(),
                clothes.getImageUrl(),
                clothes.getColor(),
                clothes.getCategory()
        );
    }
}
    public static ClothesListResDto from(ClosetSection section, List<Clothes> clothesList) {
        List<ClothesItem> items = clothesList.stream()
                .map(ClothesItem::from)
                .toList();

        return new ClothesListResDto(
                section.getSectionName(),
                items.size(),
                items
        );
    }
}
