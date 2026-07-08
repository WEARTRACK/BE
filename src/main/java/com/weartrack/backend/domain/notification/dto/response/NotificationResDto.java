package com.weartrack.backend.domain.notification.dto.response;

import com.weartrack.backend.domain.notification.entity.Notification;
import com.weartrack.backend.domain.notification.entity.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "알림 목록 항목 응답")
public record NotificationResDto(
        @Schema(description = "알림 ID", example = "1")
        Long notificationId,

        @Schema(description = "알림 타입", example = "LONG_UNWORN_CLOTHES")
        NotificationType type,

        @Schema(description = "알림 제목", example = "장기 미착용 알림")
        String title,

        @Schema(description = "알림 내용", example = "오랫동안 안 입은 옷이 8벌 있어요.")
        String body,

        @Schema(description = "읽음 여부", example = "false")
        boolean read,

        @Schema(description = "읽은 시각", example = "2026-07-07T20:00:00")
        LocalDateTime readAt,

        @Schema(description = "알림 발송 시각", example = "2026-07-07T20:00:00")
        LocalDateTime sentAt
) {

    public static NotificationResDto from(Notification notification) {
        return new NotificationResDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
