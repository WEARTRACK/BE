package com.weartrack.backend.domain.dailyReview.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.weartrack.backend.domain.dailyReview.notification.DailyReviewPushSender;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

class DailyReviewNotificationSchedulerTest {

    @Test
    void delegatesDailyReviewReminderToPushSender() {
        RecordingDailyReviewPushSender pushSender =
                new RecordingDailyReviewPushSender();
        DailyReviewNotificationScheduler scheduler =
                new DailyReviewNotificationScheduler(pushSender);

        scheduler.sendDailyReviewReminder();

        assertThat(pushSender.called).isTrue();
    }

    @Test
    void usesEightPmSeoulScheduleByDefault() throws NoSuchMethodException {
        Method method = DailyReviewNotificationScheduler.class
                .getDeclaredMethod("sendDailyReviewReminder");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertThat(scheduled.cron())
                .isEqualTo("${notification.daily-review.cron:0 0 20 * * *}");
        assertThat(scheduled.zone())
                .isEqualTo("${notification.time-zone:Asia/Seoul}");
    }

    private static class RecordingDailyReviewPushSender implements DailyReviewPushSender {

        private boolean called;

        @Override
        public void sendDailyReviewReminder() {
            called = true;
        }
    }
}
