package com.weartrack.backend.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 설정 변경 요청")
public record NotificationSettingUpdateReqDto(
        @Schema(description = "전체 푸시 알림 수신 여부", example = "true")
        Boolean pushEnabled,

        @Schema(description = "오늘의 회고 알림 수신 여부", example = "true")
        Boolean dailyReviewEnabled,

        @Schema(description = "장기 미착용 옷 알림 수신 여부", example = "true")
        Boolean longUnwornClothesEnabled,

        @Schema(description = "패션소비 리포트 알림 수신 여부", example = "false")
        Boolean fashionReportEnabled
) {
}
