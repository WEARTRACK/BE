package com.weartrack.backend.domain.dailyReview.notification;

import com.weartrack.backend.global.firebase.service.FcmMessageSender;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
            "apiPath", "/api/daily-reviews/current"
    );

    private final FcmMessageSender fcmMessageSender;

    @Value("${firebase.daily-review-topic:daily-review-reminder}")
    private String dailyReviewTopic;

    @Override
    public void sendDailyReviewReminder() {
        fcmMessageSender.sendToTopic(
                dailyReviewTopic,
                TITLE,
                BODY,
                DATA
        );
    }
}
