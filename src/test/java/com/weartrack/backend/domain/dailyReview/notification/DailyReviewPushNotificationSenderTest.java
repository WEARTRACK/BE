package com.weartrack.backend.domain.dailyReview.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.weartrack.backend.global.firebase.service.FcmMessageSender;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DailyReviewPushNotificationSenderTest {

    @Test
    void sendsDailyReviewNotificationPayloadToConfiguredTopic() {
        RecordingFcmMessageSender fcmMessageSender = new RecordingFcmMessageSender();
        DailyReviewPushNotificationSender sender =
                new DailyReviewPushNotificationSender(fcmMessageSender);
        ReflectionTestUtils.setField(
                sender,
                "dailyReviewTopic",
                "daily-review-reminder"
        );

        sender.sendDailyReviewReminder();

        assertThat(fcmMessageSender.topic).isEqualTo("daily-review-reminder");
        assertThat(fcmMessageSender.title).isEqualTo("WEARTRACK");
        assertThat(fcmMessageSender.body).isNotBlank();
        assertThat(fcmMessageSender.data).isEqualTo(Map.of(
                "type", "DAILY_REVIEW_REMINDER",
                "screen", "DAILY_REVIEW",
                "apiPath", "/api/daily-reviews/today"
        ));
    }

    private static class RecordingFcmMessageSender implements FcmMessageSender {

        private String topic;
        private String title;
        private String body;
        private Map<String, String> data;

        @Override
        public void sendToTopic(
                String topic,
                String title,
                String body,
                Map<String, String> data
        ) {
            this.topic = topic;
            this.title = title;
            this.body = body;
            this.data = data;
        }
    }
}
