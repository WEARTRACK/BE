package com.weartrack.backend.domain.member.dto.response;

public record SocialLoginResDto(
        Long memberId,
        String nickname,
        boolean profileCompleted,
        String accessToken,
        String refreshToken
) {
}
