package com.weartrack.backend.domain.weeklyReview.dto.response;

import java.time.LocalDate;
import java.util.List;

public record WeeklyReviewSummaryResDto(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        int wornClothesCount,
        int weeklyClosetUsageRate,
        String weeklyInsight,
        int longUnwornClothesCount,
        List<CategoryGroup> categories
) {
    public record CategoryGroup(
            String category,
            int wornCount,
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
