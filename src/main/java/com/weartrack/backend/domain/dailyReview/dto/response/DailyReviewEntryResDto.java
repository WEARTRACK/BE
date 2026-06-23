package com.weartrack.backend.domain.dailyReview.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DailyReviewEntryResDto(
        LocalDate reviewDate,
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        boolean completed,
        boolean previousDayIncomplete,
        boolean noRegisteredClothes,
        List<CategoryGroup> categories
) {
    public record CategoryGroup(
            String category,
            int selectedCount,
            List<ClothesItem> clothes
    ) {
    }

    public record ClothesItem(
            Long clothesId,
            String imageUrl,
            String color,
            String category,
            boolean selected
    ) {
    }
}
