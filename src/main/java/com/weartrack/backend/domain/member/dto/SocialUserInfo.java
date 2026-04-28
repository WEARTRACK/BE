package com.weartrack.backend.domain.member.dto;

import com.weartrack.backend.domain.member.constant.AuthProvider;

/**
 * 소셜 제공자에서 조회한 사용자 정보다.
 */
public record SocialUserInfo(
        AuthProvider provider,
        String providerUserId,
        String email
) {
}
