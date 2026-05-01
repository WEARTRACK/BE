package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.constant.AuthClientType;
import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SocialOAuthAuthorizeService {

    private static final String GOOGLE_AUTHORIZE_URI = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String KAKAO_AUTHORIZE_URI = "https://kauth.kakao.com/oauth/authorize";
    private static final String NAVER_AUTHORIZE_URI = "https://nid.naver.com/oauth2.0/authorize";

    private final OAuthStateService oAuthStateService;
    private final String googleClientId;
    private final String googleRedirectUri;
    private final String googleScope;
    private final String kakaoClientId;
    private final String kakaoRedirectUri;
    private final String kakaoScope;
    private final String naverClientId;
    private final String naverRedirectUri;
    private final String naverScope;

    public SocialOAuthAuthorizeService(
            OAuthStateService oAuthStateService,
            @Value("${GOOGLE_CLIENT_ID}") String googleClientId,
            @Value("${GOOGLE_REDIRECT_URI}") String googleRedirectUri,
            @Value("${oauth.google.scope:openid email profile}") String googleScope,
            @Value("${KAKAO_CLIENT_ID}") String kakaoClientId,
            @Value("${KAKAO_REDIRECT_URI}") String kakaoRedirectUri,
            @Value("${oauth.kakao.scope:account_email}") String kakaoScope,
            @Value("${NAVER_CLIENT_ID}") String naverClientId,
            @Value("${NAVER_REDIRECT_URI}") String naverRedirectUri,
            @Value("${oauth.naver.scope:name email}") String naverScope
    ) {
        this.oAuthStateService = oAuthStateService;
        this.googleClientId = googleClientId;
        this.googleRedirectUri = googleRedirectUri;
        this.googleScope = googleScope;
        this.kakaoClientId = kakaoClientId;
        this.kakaoRedirectUri = kakaoRedirectUri;
        this.kakaoScope = kakaoScope;
        this.naverClientId = naverClientId;
        this.naverRedirectUri = naverRedirectUri;
        this.naverScope = naverScope;
    }

    public URI buildAuthorizeUri(AuthProvider provider, AuthClientType clientType) {
        String state = oAuthStateService.create(provider, clientType);

        return switch (provider) {
            case GOOGLE -> UriComponentsBuilder.fromUriString(GOOGLE_AUTHORIZE_URI)
                    .queryParam("client_id", googleClientId)
                    .queryParam("redirect_uri", googleRedirectUri)
                    .queryParam("response_type", "code")
                    .queryParam("scope", googleScope)
                    .queryParam("state", state)
                    .build()
                    .encode()
                    .toUri();
            case KAKAO -> UriComponentsBuilder.fromUriString(KAKAO_AUTHORIZE_URI)
                    .queryParam("client_id", kakaoClientId)
                    .queryParam("redirect_uri", kakaoRedirectUri)
                    .queryParam("response_type", "code")
                    .queryParam("scope", kakaoScope)
                    .queryParam("state", state)
                    .build()
                    .encode()
                    .toUri();
            case NAVER -> UriComponentsBuilder.fromUriString(NAVER_AUTHORIZE_URI)
                    .queryParam("client_id", naverClientId)
                    .queryParam("redirect_uri", naverRedirectUri)
                    .queryParam("response_type", "code")
                    .queryParam("scope", naverScope)
                    .queryParam("state", state)
                    .build()
                    .encode()
                    .toUri();
            default -> throw new GeneralException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        };
    }
}
