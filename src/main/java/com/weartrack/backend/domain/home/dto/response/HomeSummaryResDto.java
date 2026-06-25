package com.weartrack.backend.domain.home.dto.response;

public record HomeSummaryResDto(
        long totalClothesCount,
        long weeklyExpenseAmount,
        int weeklyClosetUsageRate,
        long closetCount,
        long storageCount
) {
}