package com.weartrack.backend.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameSetReqDto(
        @NotBlank
        @Size(max = 30)
        String nickname
) {
}
