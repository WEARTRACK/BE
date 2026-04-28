package com.weartrack.backend.domain.member.dto.response;

/**
 * 소셜 로그인 완료 후 내려주는 응답이다.
 */
public record SocialLoginResDto(
        Long memberId,
        String nickname,
        boolean profileCompleted,
        String accessToken,
        String refreshToken
) {
}
