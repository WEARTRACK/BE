package com.weartrack.backend.domain.dailyReview.scheduler;

import com.weartrack.backend.domain.dailyReview.notification.DailyReviewPushSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyReviewNotificationScheduler {

    private final DailyReviewPushSender dailyReviewPushSender;

    @Scheduled(cron = "0 0 20 * * *", zone = "Asia/Seoul")
    public void sendDailyReviewReminder() {
        dailyReviewPushSender.sendDailyReviewReminder();
    }
}
