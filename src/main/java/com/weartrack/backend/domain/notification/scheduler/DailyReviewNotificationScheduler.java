package com.weartrack.backend.domain.notification.scheduler;

import com.weartrack.backend.domain.notification.sender.DailyReviewPushSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyReviewNotificationScheduler {

    private final DailyReviewPushSender dailyReviewPushSender;

    @Scheduled(
            cron = "${notification.daily-review.cron:0 0 20 * * *}",
            zone = "${notification.time-zone:Asia/Seoul}"
    )
    public void sendDailyReviewReminder() {
        dailyReviewPushSender.sendDailyReviewReminder();
    }
}
