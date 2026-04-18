package com.weartrack.backend.domain.member.dto.response;

public record NicknameSetResDto(
        Long memberId,
        String nickname,
        boolean profileCompleted
) {
}
