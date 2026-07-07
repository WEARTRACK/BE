package com.weartrack.backend.domain.notification.sender;

import java.time.LocalDate;

public interface WeeklyFashionReportPushSender {

    void sendWeeklyFashionReport(
            LocalDate weekStartDate,
            LocalDate weekEndDate
    );
}
