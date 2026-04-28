package com.weartrack.backend.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.weartrack.backend.global.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET = "weartrack-local-jwt-secret-key-2026-sufficiently-long";

    @Test
    @DisplayName("access token 생성 후 memberId를 다시 추출할 수 있다.")
    void createAndExtractAccessToken() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 3600L, 1209600L);

        String accessToken = jwtTokenProvider.createAccessToken(1L);

        Long memberId = jwtTokenProvider.extractMemberId(accessToken);

        assertThat(memberId).isEqualTo(1L);
    }

    @Test
    @DisplayName("refresh token은 API 인증용 memberId 추출에 사용할 수 없다.")
    void extractMemberIdRejectsRefreshToken() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 3600L, 1209600L);

        String refreshToken = jwtTokenProvider.createRefreshToken(2L);

        assertThatThrownBy(() -> jwtTokenProvider.extractMemberId(refreshToken))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    @DisplayName("잘못된 JWT는 GeneralException을 발생시킨다.")
    void extractMemberIdFail() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 3600L, 1209600L);

        assertThatThrownBy(() -> jwtTokenProvider.extractMemberId("invalid-token"))
                .isInstanceOf(GeneralException.class);
    }
}
