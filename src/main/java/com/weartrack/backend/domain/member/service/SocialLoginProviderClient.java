package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.SocialUserInfo;

public interface SocialLoginProviderClient {

    AuthProvider supports();

    SocialUserInfo getUserInfo(String authorizationCode, String state);
}
