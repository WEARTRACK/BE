package com.weartrack.backend.domain.dailyreview.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DailyReviewSaveReqDto(
        @NotNull(message = "착용 옷 목록은 필수입니다.")
        List<Long> clothesIds
) {
}
