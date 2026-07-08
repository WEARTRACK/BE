package com.weartrack.backend.domain.notification.dto.response;

import com.weartrack.backend.domain.notification.entity.NotificationSetting;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 설정 응답")
public record NotificationSettingResDto(
        @Schema(description = "전체 푸시 알림 수신 여부", example = "true")
        boolean pushEnabled,

        @Schema(description = "오늘의 회고 알림 수신 여부", example = "true")
        boolean dailyReviewEnabled,

        @Schema(description = "장기 미착용 옷 알림 수신 여부", example = "true")
        boolean longUnwornClothesEnabled,

        @Schema(description = "패션소비 리포트 알림 수신 여부", example = "false")
        boolean fashionReportEnabled
) {
    public static NotificationSettingResDto from(NotificationSetting setting) {
        return new NotificationSettingResDto(
                setting.isPushEnabled(),
                setting.isDailyReviewEnabled(),
                setting.isLongUnwornClothesEnabled(),
                setting.isFashionReportEnabled()
        );
    }
}
