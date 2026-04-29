package com.weartrack.backend.domain.home.dto;

public record HomeSummaryResDto(
        long totalClothesCount,
        long weeklyExpenseAmount,
        int weeklyClosetUsageRate
) {
}