package com.weartrack.backend.domain.member.dto.request;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 소셜 로그인 요청 값이다.
 */
public record SocialLoginReqDto(
        @NotNull
        AuthProvider provider,

        @NotBlank
        String authorizationCode,

        String state
) {
}
