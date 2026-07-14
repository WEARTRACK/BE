package com.weartrack.backend.domain.purchaseCheck.dto.response;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

public record PurchaseCheckResDto(
        String message,
        Integer totalCount,
        Integer currentPage,
        Integer totalPages,
        Boolean hasNext,
        List<SimilarClothesItem> clothes
) {

    public record SimilarClothesItem(
            Long clothesId,
            String imageUrl,
            String productName,
            String closetName,
            String sectionName,
            String color,
            String category
    ) {
        public static SimilarClothesItem from(
                Clothes clothes,
                String closetName,
                String sectionName
        ) {
            return new SimilarClothesItem(
                    clothes.getId(),
                    clothes.getImageUrl(),
                    clothes.getProductName(),
                    closetName,
                    sectionName,
                    clothes.getColor(),
                    clothes.getCategory()
            );
        }
    }

    public static PurchaseCheckResDto from(
            String message,
            Page<Clothes> page,
            Map<Long, String> closetNameMap,
            Map<Long, String> sectionNameMap
    ) {
        List<SimilarClothesItem> items = page.getContent().stream()
                .map(clothes -> SimilarClothesItem.from(
                        clothes,
                        closetNameMap.getOrDefault(clothes.getClosetSectionId(), ""),
                        sectionNameMap.getOrDefault(clothes.getClosetSectionId(), "")
                ))
                .toList();

        return new PurchaseCheckResDto(
                message,
                (int) page.getTotalElements(),
                page.getNumber(),
                page.getTotalPages(),
                page.hasNext(),
                items
        );
    }
}
