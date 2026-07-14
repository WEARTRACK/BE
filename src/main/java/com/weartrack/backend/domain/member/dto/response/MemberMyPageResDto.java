package com.weartrack.backend.domain.member.dto.response;

public record MemberMyPageResDto(
        Long memberId,
        String nickname,
        String email
) {
}
