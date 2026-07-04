package com.weartrack.backend.domain.notification.sender;

import com.weartrack.backend.domain.notification.entity.enums.NotificationType;
import com.weartrack.backend.domain.notification.service.NotificationService;
import com.weartrack.backend.global.firebase.service.FcmMessageSender;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyReviewPushNotificationSender implements DailyReviewPushSender {

    private static final String TITLE = "WEARTRACK";
    private static final String BODY =
            "오늘은 어떤 옷을 입었나요? 기록 후 패션소비 리포트를 확인해보세요.";
    private static final Map<String, String> DATA = Map.of(
            "type", "DAILY_REVIEW_REMINDER",
            "screen", "DAILY_REVIEW",
            "apiPath", "/api/daily-reviews/today"
    );

    private final FcmMessageSender fcmMessageSender;
    private final NotificationService notificationService;

    @Override
    public void sendDailyReviewReminder() {
        List<String> tokens = notificationService.findTokensEnabledFor(NotificationType.DAILY_REVIEW);

        fcmMessageSender.sendToTokens(
                tokens,
                TITLE,
                BODY,
                DATA
        );
    }
}
