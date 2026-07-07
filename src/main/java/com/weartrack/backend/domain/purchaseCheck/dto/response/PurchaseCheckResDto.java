package com.weartrack.backend.domain.purchaseCheck.dto.response;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import java.util.List;
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
            String color,
            String category
    ) {
        public static SimilarClothesItem from(Clothes clothes) {
            return new SimilarClothesItem(
                    clothes.getId(),
                    clothes.getImageUrl(),
                    clothes.getProductName(),
                    clothes.getColor(),
                    clothes.getCategory()
            );
        }
    }

    public static PurchaseCheckResDto from(String message, Page<Clothes> page) {
        List<SimilarClothesItem> items = page.getContent().stream()
                .map(SimilarClothesItem::from)
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
