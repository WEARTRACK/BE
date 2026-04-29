package com.weartrack.backend.domain.member.dto.response;

public record NicknameAvailabilityCheckResDto(
        String nickname,
        boolean available
) {
}
