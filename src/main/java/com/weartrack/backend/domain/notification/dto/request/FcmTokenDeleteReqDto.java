package com.weartrack.backend.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "FCM 토큰 삭제 요청")
public record FcmTokenDeleteReqDto(
        @Schema(description = "삭제할 FCM 디바이스 토큰", example = "fcm-token-value")
        @NotBlank
        String token
) {
}
