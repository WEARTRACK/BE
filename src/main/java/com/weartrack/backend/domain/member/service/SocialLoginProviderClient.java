package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.global.exception.GeneralException;

/**
 * 소셜 로그인 제공자별 사용자 정보 조회를 추상화한다.
 */
public interface SocialLoginProviderClient {

    /**
     * 현재 구현체가 처리하는 제공자를 반환한다.
     */
    AuthProvider supports();

    /**
     * 인가 코드를 사용자 정보로 교환한다.
     */
    SocialUserInfo getUserInfo(String authorizationCode, String state);

    default SocialUserInfo getUserInfoByAccessToken(String accessToken) {
        throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_LOGIN_REQUEST);
    }

    default SocialUserInfo getUserInfoByIdToken(String idToken) {
        throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_LOGIN_REQUEST);
    }
}
