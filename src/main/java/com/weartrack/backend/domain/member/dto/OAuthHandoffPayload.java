package com.weartrack.backend.domain.member.dto;

import com.weartrack.backend.domain.member.constant.AuthProvider;

public record OAuthHandoffPayload(
        AuthProvider provider,
        String authorizationCode,
        String state
) {
}
