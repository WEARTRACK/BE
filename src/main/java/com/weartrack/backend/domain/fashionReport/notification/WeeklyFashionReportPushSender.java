package com.weartrack.backend.domain.fashionReport.notification;

import java.time.LocalDate;

public interface WeeklyFashionReportPushSender {

    void sendWeeklyFashionReport(
            LocalDate weekStartDate,
            LocalDate weekEndDate
    );
}
