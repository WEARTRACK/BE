package com.weartrack.backend.domain.clothes.dto.response;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public record ClothesFilterResDto(
        Integer totalCount,
        Integer currentPage,
        Integer totalPages,
        Boolean hasNext,
        List<FilteredClothesItem> clothes
) {
    public record FilteredClothesItem(
            Long clothesId,
            String imageUrl,
            String color,
            String category,
            String sectionName
    ) {
        public static FilteredClothesItem from(Clothes clothes, String sectionName) {
            return new FilteredClothesItem(
                    clothes.getId(),
                    clothes.getImageUrl(),
                    clothes.getColor(),
                    clothes.getCategory(),
                    sectionName
            );
        }
    }

    public static ClothesFilterResDto from(Page<Clothes> page, Map<Long, String> sectionNameMap) {
        List<FilteredClothesItem> items = page.getContent().stream()
                .map(c -> FilteredClothesItem.from(
                        c,
                        sectionNameMap.getOrDefault(c.getClosetSectionId(), "")
                ))
                .toList();

        return new ClothesFilterResDto(
                (int) page.getTotalElements(),
                page.getNumber(),
                page.getTotalPages(),
                page.hasNext(),
                items
        );
    }
}
