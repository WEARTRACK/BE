package com.weartrack.backend.domain.member.dto.response;

public record TokenRefreshResDto(
        String accessToken,
        String refreshToken
) {
}
