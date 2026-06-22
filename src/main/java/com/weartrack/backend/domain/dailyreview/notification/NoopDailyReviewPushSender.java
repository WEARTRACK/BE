package com.weartrack.backend.domain.dailyreview.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoopDailyReviewPushSender implements DailyReviewPushSender {

    @Override
    public void sendDailyReviewReminder() {
        log.info("Daily review reminder requested. Push provider is not configured yet.");
    }
}
