package com.weartrack.backend.domain.fashionReport.notification;

import com.weartrack.backend.global.firebase.service.FcmMessageSender;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyFashionReportPushNotificationSender
        implements WeeklyFashionReportPushSender {

    private static final String TITLE = "WEARTRACK";
    private static final String BODY =
            "이번 주 패션 지출 리포트가 도착했어요. 지금 확인해보세요.";
    private static final String API_PATH_PREFIX =
            "/api/fashion-consumption/reports/weekly/";

    private final FcmMessageSender fcmMessageSender;

    @Value("${firebase.weekly-fashion-report-topic:weekly-fashion-report}")
    private String weeklyFashionReportTopic;

    @Override
    public void sendWeeklyFashionReport(
            LocalDate weekStartDate,
            LocalDate weekEndDate
    ) {
        String weekStartDateValue = weekStartDate.toString();

        fcmMessageSender.sendToTopic(
                weeklyFashionReportTopic,
                TITLE,
                BODY,
                Map.of(
                        "type", "WEEKLY_FASHION_REPORT",
                        "screen", "WEEKLY_FASHION_REPORT",
                        "weekStartDate", weekStartDateValue,
                        "weekEndDate", weekEndDate.toString(),
                        "apiPath", API_PATH_PREFIX + weekStartDateValue
                )
        );
    }
}
