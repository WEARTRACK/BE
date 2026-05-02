package com.weartrack.backend.domain.member.dto;

import com.weartrack.backend.domain.member.constant.AuthClientType;
import com.weartrack.backend.domain.member.constant.AuthProvider;

public record OAuthStatePayload(
        AuthProvider provider,
        AuthClientType clientType
) {
}
