package com.weartrack.backend.domain.fashionReport.scheduler;

import com.weartrack.backend.domain.fashionReport.notification.WeeklyFashionReportPushSender;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyFashionReportNotificationScheduler {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final WeeklyFashionReportPushSender weeklyFashionReportPushSender;

    @Scheduled(
            cron = "${notification.weekly-fashion-report.cron:0 0 21 * * SUN}",
            zone = "${notification.time-zone:Asia/Seoul}"
    )
    public void sendPreviousWeeklyFashionReport() {
        LocalDate currentWeekStartDate = LocalDate.now(SEOUL_ZONE)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate reportWeekStartDate = currentWeekStartDate.minusWeeks(1);
        LocalDate reportWeekEndDate = reportWeekStartDate.plusDays(6);

        log.info(
                "주간 패션 리포트 알림을 요청합니다. weekStartDate={}, weekEndDate={}",
                reportWeekStartDate,
                reportWeekEndDate
        );

        weeklyFashionReportPushSender.sendWeeklyFashionReport(
                reportWeekStartDate,
                reportWeekEndDate
        );
    }
}
