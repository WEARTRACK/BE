package com.weartrack.backend.domain.notification.dto.request;

import com.weartrack.backend.domain.notification.entity.enums.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "FCM 토큰 등록 요청")
public record FcmTokenRegisterReqDto(
        @Schema(description = "Firebase에서 발급받은 FCM 디바이스 토큰", example = "fcm-token-value")
        @NotBlank
        String token,

        @Schema(description = "토큰이 발급된 기기 타입", example = "ANDROID")
        DeviceType deviceType
) {
    public DeviceType deviceTypeOrUnknown() {
        return deviceType == null ? DeviceType.UNKNOWN : deviceType;
    }
}
