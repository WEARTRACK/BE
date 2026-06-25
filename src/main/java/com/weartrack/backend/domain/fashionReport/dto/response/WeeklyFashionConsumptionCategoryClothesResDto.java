package com.weartrack.backend.domain.fashionReport.dto.response;

import java.time.LocalDate;
import java.util.List;

public record WeeklyFashionConsumptionCategoryClothesResDto(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        String category,
        List<ClothesItem> clothes
) {
    public record ClothesItem(
            Long clothesId,
            String imageUrl,
            String productName,
            String sourceShopName,
            Integer price
    ) {
    }
}
