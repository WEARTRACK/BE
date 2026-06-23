package com.weartrack.backend.domain.dailyReview.dto.response;

import com.weartrack.backend.domain.weeklyReview.dto.response.WeeklyReviewSummaryResDto;
import java.time.LocalDate;

public record DailyReviewSaveResDto(
        LocalDate reviewDate,
        int wornClothesCount,
        boolean zeroSelected,
        WeeklyReviewSummaryResDto weeklyReview
) {
}
