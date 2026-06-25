package com.weartrack.backend.domain.home.dto.response;

import java.time.LocalDate;

public record HomeWeeklyClosetUsageAnalysisResDto(
        LocalDate weekStartDate,
        LocalDate weekEndDate,
        int weeklyClosetUsageRate,
        String closetUsageType,
        long unwornClothesCount
) {
}
