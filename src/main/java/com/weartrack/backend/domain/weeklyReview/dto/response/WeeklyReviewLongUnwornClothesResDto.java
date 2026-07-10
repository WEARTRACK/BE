package com.weartrack.backend.domain.weeklyReview.dto.response;

import java.time.LocalDate;
import java.util.List;

public record WeeklyReviewLongUnwornClothesResDto(
        LocalDate periodStartDate,
        LocalDate periodEndDate,
        int longUnwornClothesCount,
        List<CategoryGroup> categories
) {
    public record CategoryGroup(
            String category,
            int unwornCount,
            List<ClothesItem> clothes
    ) {
    }

    public record ClothesItem(
            Long clothesId,
            String imageUrl,
            String color
    ) {
    }
}
