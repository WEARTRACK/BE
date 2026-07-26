package com.weartrack.backend.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshReqDto(
        @NotBlank
        String refreshToken
) {
}
