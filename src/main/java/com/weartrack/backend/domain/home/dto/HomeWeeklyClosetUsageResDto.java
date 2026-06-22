package com.weartrack.backend.domain.home.dto;

import java.time.LocalDate;
import java.util.List;

public record HomeWeeklyClosetUsageResDto(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        LocalDate reviewDate,
        int weeklyClosetUsageRate,
        String closetUsageType,
        String closetUsageLabel,
        String closetUsageRange,
        long unwornClothesCount,
        String closetUsageMessage,
        int wornClothesCount,
        long totalWornClothesPrice,
        List<WornClothesItem> wornClothes
) {
    public record WornClothesItem(
            Long clothesId,
            String imageUrl,
            Integer price
    ) {
    }
}
