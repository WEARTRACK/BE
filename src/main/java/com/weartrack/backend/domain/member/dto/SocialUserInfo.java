package com.weartrack.backend.domain.member.dto;

import com.weartrack.backend.domain.member.constant.AuthProvider;

public record SocialUserInfo(
        AuthProvider provider,
        String providerUserId,
        String email
) {
}
