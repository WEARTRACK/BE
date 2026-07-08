package com.weartrack.backend.domain.clothes.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClothesCreateResponse(
        Long clothesId,
        Long photoId,
        String imageUrl,
        String productName,
        String brandName,
        String color,
        String category,
        Integer price,
        LocalDate purchaseDate,
        String storageLocation,
        Long closetId,
        Long sectionId,
        LocalDateTime createdAt
) {

    // 기존 ProductLinkService 호환용 생성자
    // 링크 등록 담당 코드의 기존 11개 인자 호출을 깨지 않기 위한 용도
    public ClothesCreateResponse(
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
        this(
                clothesId,
                photoId,
                imageUrl,
                productName,
                null,
                color,
                category,
                price,
                purchaseDate,
                storageLocation,
                null,
                sectionId,
                createdAt
        );
    }
}