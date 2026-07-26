package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.OAuthHandoffPayload;
import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.dto.request.SocialLoginReqDto;
import com.weartrack.backend.domain.member.dto.request.TokenRefreshReqDto;
import com.weartrack.backend.domain.member.dto.response.SocialLoginResDto;
import com.weartrack.backend.domain.member.dto.response.TokenRefreshResDto;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final Map<AuthProvider, SocialLoginProviderClient> providerClients;
    private final AuthLoginTransactionService authLoginTransactionService;
    private final OAuthHandoffService oAuthHandoffService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            List<SocialLoginProviderClient> providerClients,
            AuthLoginTransactionService authLoginTransactionService,
            OAuthHandoffService oAuthHandoffService,
            RefreshTokenService refreshTokenService
    ) {
        this.providerClients = mapProviderClients(providerClients);
        this.authLoginTransactionService = authLoginTransactionService;
        this.oAuthHandoffService = oAuthHandoffService;
        this.refreshTokenService = refreshTokenService;
    }

    public SocialLoginResDto login(SocialLoginReqDto request) {
        if (StringUtils.hasText(request.handoffToken())) {
            OAuthHandoffPayload handoffPayload = oAuthHandoffService.consume(request.provider(), request.handoffToken());
            return loginInternal(
                    handoffPayload.provider(),
                    handoffPayload.authorizationCode(),
                    handoffPayload.state()
            );
        }

        if (!StringUtils.hasText(request.authorizationCode())) {
            if (StringUtils.hasText(request.accessToken())) {
                return loginWithAccessToken(request.provider(), request.accessToken());
            }

            if (StringUtils.hasText(request.idToken())) {
                return loginWithIdToken(request.provider(), request.idToken());
            }

            throw new GeneralException(AuthErrorCode.INVALID_SOCIAL_LOGIN_REQUEST);
        }

        return loginInternal(request.provider(), request.authorizationCode(), request.state());
    }

    public SocialLoginResDto login(
            AuthProvider provider,
            String authorizationCode,
            String state
    ) {
        return loginInternal(provider, authorizationCode, state);
    }

    public SocialLoginResDto login(
            AuthProvider provider,
            String authorizationCode,
            String state,
            String expectedState
    ) {
        validateState(state, expectedState);
        return loginInternal(provider, authorizationCode, state);
    }

    public TokenRefreshResDto refresh(TokenRefreshReqDto request) {
        return refreshTokenService.rotate(request.refreshToken());
    }

    private SocialLoginResDto loginInternal(
            AuthProvider provider,
            String authorizationCode,
            String state
    ) {
        SocialLoginProviderClient providerClient = providerClients.get(provider);
        if (providerClient == null) {
            throw new GeneralException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        }

        SocialUserInfo socialUserInfo = providerClient.getUserInfo(authorizationCode, state);
        return authLoginTransactionService.loginOrRegister(socialUserInfo);
    }

    private SocialLoginResDto loginWithAccessToken(AuthProvider provider, String accessToken) {
        SocialLoginProviderClient providerClient = getProviderClient(provider);
        SocialUserInfo socialUserInfo = providerClient.getUserInfoByAccessToken(accessToken);
        return authLoginTransactionService.loginOrRegister(socialUserInfo);
    }

    private SocialLoginResDto loginWithIdToken(AuthProvider provider, String idToken) {
        SocialLoginProviderClient providerClient = getProviderClient(provider);
        SocialUserInfo socialUserInfo = providerClient.getUserInfoByIdToken(idToken);
        return authLoginTransactionService.loginOrRegister(socialUserInfo);
    }

    private SocialLoginProviderClient getProviderClient(AuthProvider provider) {
        SocialLoginProviderClient providerClient = providerClients.get(provider);
        if (providerClient == null) {
            throw new GeneralException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        }
        return providerClient;
    }

    private void validateState(String state, String expectedState) {
        if (!StringUtils.hasText(state) || !StringUtils.hasText(expectedState)) {
            throw new GeneralException(AuthErrorCode.INVALID_OAUTH_STATE);
        }

        boolean matches = MessageDigest.isEqual(
                state.getBytes(StandardCharsets.UTF_8),
                expectedState.getBytes(StandardCharsets.UTF_8)
        );

        if (!matches) {
            throw new GeneralException(AuthErrorCode.INVALID_OAUTH_STATE);
        }
    }

    private Map<AuthProvider, SocialLoginProviderClient> mapProviderClients(List<SocialLoginProviderClient> providerClients) {
        Map<AuthProvider, SocialLoginProviderClient> mappedClients = new EnumMap<>(AuthProvider.class);
        for (SocialLoginProviderClient providerClient : providerClients) {
            mappedClients.put(providerClient.supports(), providerClient);
        }
        return mappedClients;
    }
}
