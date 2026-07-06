package com.weartrack.backend.domain.notification.sender;

import com.weartrack.backend.domain.notification.entity.enums.NotificationType;
import com.weartrack.backend.domain.notification.service.NotificationService;
import com.weartrack.backend.global.firebase.service.FcmMessageSender;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
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
    private final NotificationService notificationService;

    @Override
    public void sendWeeklyFashionReport(
            LocalDate weekStartDate,
            LocalDate weekEndDate
    ) {
        String weekStartDateValue = weekStartDate.toString();
        Map<Long, List<String>> tokenMap =
                notificationService.findTokenMapEnabledFor(NotificationType.FASHION_REPORT);
        Map<String, String> data = Map.of(
                "type", "WEEKLY_FASHION_REPORT",
                "screen", "WEEKLY_FASHION_REPORT",
                "weekStartDate", weekStartDateValue,
                "weekEndDate", weekEndDate.toString(),
                "apiPath", API_PATH_PREFIX + weekStartDateValue
        );

        tokenMap.forEach((memberId, tokens) -> {
            fcmMessageSender.sendToTokens(
                    tokens,
                    TITLE,
                    BODY,
                    data
            );
            notificationService.saveNotification(
                    memberId,
                    NotificationType.FASHION_REPORT,
                    TITLE,
                    BODY
            );
        });
    }
}
