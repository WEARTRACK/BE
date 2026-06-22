package com.weartrack.backend.domain.dailyreview.dto.response;

import com.weartrack.backend.domain.weeklyreview.dto.response.WeeklyReviewSummaryResDto;
import java.time.LocalDate;

public record DailyReviewSaveResDto(
        LocalDate reviewDate,
        int wornClothesCount,
        boolean zeroSelected,
        WeeklyReviewSummaryResDto weeklyReview
) {
}
