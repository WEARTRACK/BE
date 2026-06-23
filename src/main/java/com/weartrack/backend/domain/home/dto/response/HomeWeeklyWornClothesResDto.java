package com.weartrack.backend.domain.home.dto.response;

import java.util.List;

public record HomeWeeklyWornClothesResDto(
        int weeklyClosetUsageRate,
        String closetUsageType,
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
