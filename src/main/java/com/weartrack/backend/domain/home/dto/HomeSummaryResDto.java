package com.weartrack.backend.domain.home.dto;

public record HomeSummaryResDto(
        int totalClothesCount,
        int weeklyExpenseAmount,
        int weeklyClosetUsageRate
) {
}