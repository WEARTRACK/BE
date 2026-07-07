package com.weartrack.backend.domain.notification.scheduler;

import com.weartrack.backend.domain.notification.service.LongUnwornClothesNotificationService;
import java.time.YearMonth;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LongUnwornClothesNotificationScheduler {

    private final LongUnwornClothesNotificationService notificationService;
    private final ZoneId notificationZoneId;

    public LongUnwornClothesNotificationScheduler(
            LongUnwornClothesNotificationService notificationService,
            @Value("${notification.time-zone:Asia/Seoul}") String notificationTimeZone
    ) {
        this.notificationService = notificationService;
        this.notificationZoneId = ZoneId.of(notificationTimeZone);
    }

    @Scheduled(
            cron = "${notification.long-unworn-clothes.cron:0 0 20 1 * *}",
            zone = "${notification.time-zone:Asia/Seoul}"
    )
    public void sendPreviousMonthLongUnwornClothesNotifications() {
        YearMonth targetMonth = YearMonth.now(notificationZoneId).minusMonths(1);

        log.info("장기 미착용 옷 알림 발송을 요청합니다. targetMonth={}", targetMonth);
        notificationService.sendMonthlyLongUnwornClothesNotifications(targetMonth);
    }
}
