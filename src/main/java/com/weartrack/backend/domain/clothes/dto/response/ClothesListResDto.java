package com.weartrack.backend.domain.clothes.dto.response;

import com.weartrack.backend.domain.closet.entity.ClosetSection;
import com.weartrack.backend.domain.clothes.entity.Clothes;
import org.springframework.data.domain.Page;

import java.util.List;

public record ClothesListResDto(
        String sectionName,
        Integer totalCount,
        Integer currentPage,
        Integer totalPages,
        Boolean hasNext,
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
    public static ClothesListResDto from(ClosetSection section, Page<Clothes> page) {
        List<ClothesItem> items = page.getContent().stream()
                .map(ClothesItem::from)
                .toList();

        return new ClothesListResDto(
                section.getSectionName(),
                (int) page.getTotalElements(),
                page.getNumber(),
                page.getTotalPages(),
                page.hasNext(),
                items
        );
    }
}
