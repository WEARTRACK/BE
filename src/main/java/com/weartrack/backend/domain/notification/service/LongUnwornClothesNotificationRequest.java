package com.weartrack.backend.domain.notification.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public record LongUnwornClothesNotificationRequest(
        Long memberId,
        YearMonth targetMonth,
        LocalDate periodStartDate,
        LocalDate periodEndDate,
        long unwornClothesCount,
        List<String> tokens
) {
}
