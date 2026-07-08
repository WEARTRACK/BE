package com.weartrack.backend.domain.notification.service;

import com.weartrack.backend.domain.notification.entity.enums.NotificationType;
import com.weartrack.backend.global.firebase.service.FcmMessageSender;
import java.time.YearMonth;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LongUnwornClothesNotificationService {

    private static final String TITLE = "WEARTRACK";
    private static final String BODY_FORMAT = "오랫동안 안 입은 옷이 %d벌 있어요! 클릭해서 확인해보세요.";

    private final LongUnwornClothesNotificationQueryService queryService;
    private final FcmMessageSender fcmMessageSender;
    private final NotificationService notificationService;

    public void sendMonthlyLongUnwornClothesNotifications(YearMonth targetMonth) {
        queryService.findNotificationRequests(targetMonth)
                .forEach(this::sendNotificationSafely);
    }

    private void sendNotificationSafely(LongUnwornClothesNotificationRequest request) {
        try {
            sendNotification(request);
        } catch (Exception e) {
            log.error(
                    "장기 미착용 옷 알림 발송 처리 실패: memberId={}, targetMonth={}",
                    request.memberId(),
                    request.targetMonth(),
                    e
            );
        }
    }

    private void sendNotification(LongUnwornClothesNotificationRequest request) {
        Map<String, String> data = Map.of(
                "type", "LONG_UNWORN_CLOTHES",
                "screen", "LONG_UNWORN_CLOTHES",
                "targetMonth", request.targetMonth().toString(),
                "periodStartDate", request.periodStartDate().toString(),
                "periodEndDate", request.periodEndDate().toString(),
                "unwornClothesCount", String.valueOf(request.unwornClothesCount())
        );

        fcmMessageSender.sendToTokens(
                request.tokens(),
                TITLE,
                BODY_FORMAT.formatted(request.unwornClothesCount()),
                data
        );
        notificationService.saveNotification(
                request.memberId(),
                NotificationType.LONG_UNWORN_CLOTHES,
                TITLE,
                BODY_FORMAT.formatted(request.unwornClothesCount())
        );
        log.info(
                "장기 미착용 옷 알림 발송 요청: memberId={}, targetMonth={}, unwornClothesCount={}",
                request.memberId(),
                request.targetMonth(),
                request.unwornClothesCount()
        );
    }
}
