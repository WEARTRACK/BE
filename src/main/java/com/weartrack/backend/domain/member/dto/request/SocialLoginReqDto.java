package com.weartrack.backend.domain.member.dto.request;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SocialLoginReqDto(
        @NotNull
        AuthProvider provider,

        @NotBlank
        String authorizationCode,

        String state
) {
}
