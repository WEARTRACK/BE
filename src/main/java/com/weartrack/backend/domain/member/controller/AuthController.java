package com.weartrack.backend.domain.member.controller;

import com.weartrack.backend.domain.member.constant.AuthClientType;
import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.OAuthStatePayload;
import com.weartrack.backend.domain.member.dto.request.SocialLoginReqDto;
import com.weartrack.backend.domain.member.dto.response.SocialLoginResDto;
import com.weartrack.backend.domain.member.service.AuthService;
import com.weartrack.backend.domain.member.service.OAuthHandoffService;
import com.weartrack.backend.domain.member.service.OAuthStateService;
import com.weartrack.backend.domain.member.service.SocialOAuthAuthorizeService;
import com.weartrack.backend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private static final String OAUTH_STATE_COOKIE_NAME = "oauth-login-state";

    private final AuthService authService;
    private final OAuthHandoffService oAuthHandoffService;
    private final OAuthStateService oAuthStateService;
    private final SocialOAuthAuthorizeService socialOAuthAuthorizeService;

    @Value("${app.oauth.mobile.callback-base-uri:weartrack://auth/callback}")
    private String mobileCallbackBaseUri;

    @GetMapping("/api/auth/social/authorize/{provider}")
    public ResponseEntity<Void> authorize(
            @PathVariable String provider,
            @RequestParam(name = "client", defaultValue = "MOBILE") AuthClientType clientType
    ) {
        AuthProvider authProvider = AuthProvider.valueOf(provider.toUpperCase());
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(socialOAuthAuthorizeService.buildAuthorizeUri(authProvider, clientType))
                .build();
    }


    /**
     * 프론트가 전달한 소셜 authorization code를 기반으로 자체 JWT를 발급합니다.
     */
    @PostMapping("/api/auth/social/login")
    public ApiResponse<SocialLoginResDto> socialLogin(
            @Valid @RequestBody SocialLoginReqDto request,
            HttpServletResponse httpResponse
    ) {
        try {
            SocialLoginResDto response = authService.login(request);
            return ApiResponse.success(response);
        } finally {
            expireOAuthStateCookie(httpResponse);
        }
    }


    /**
     * Google 로그인 완료 후 전달된 인가 코드를 바로 처리합니다.
     */
    @GetMapping("/login/oauth2/code/google")
    public ResponseEntity<?> googleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        return handleCallback(AuthProvider.GOOGLE, code, state, httpRequest, httpResponse);
    }


    /**
     * Kakao 로그인 완료 후 전달된 인가 코드를 바로 처리합니다.
     */
    @GetMapping("/login/oauth2/code/kakao")
    public ResponseEntity<?> kakaoCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        return handleCallback(AuthProvider.KAKAO, code, state, httpRequest, httpResponse);
    }


    /**
     * Naver 로그인 완료 후 전달된 인가 코드와 state를 바로 처리합니다.
     */
    @GetMapping("/login/oauth2/code/naver")
    public ResponseEntity<?> naverCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        return handleCallback(AuthProvider.NAVER, code, state, httpRequest, httpResponse);
    }

    private ResponseEntity<?> handleCallback(
            AuthProvider provider,
            String code,
            String state,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        try {
            var managedState = oAuthStateService.consumeIfPresent(provider, state);
            if (managedState.isPresent()) {
                OAuthStatePayload statePayload = managedState.get();
                if (statePayload.clientType() == AuthClientType.MOBILE) {
                    String handoffToken = oAuthHandoffService.create(provider, code, state);
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .location(buildMobileCallbackUri(provider, handoffToken))
                            .build();
                }
            }

            SocialLoginResDto response = authService.login(
                    provider,
                    code,
                    state,
                    extractOAuthState(httpRequest)
            );
            return ResponseEntity.ok(ApiResponse.success(response));
        } finally {
            expireOAuthStateCookie(httpResponse);
        }
    }

    private URI buildMobileCallbackUri(AuthProvider provider, String handoffToken) {
        return URI.create("%s/%s?handoff=%s".formatted(
                mobileCallbackBaseUri,
                provider.name().toLowerCase(),
                handoffToken
        ));
    }

    private String extractOAuthState(HttpServletRequest request) {
        var stateCookie = WebUtils.getCookie(request, OAUTH_STATE_COOKIE_NAME);
        return stateCookie == null ? null : stateCookie.getValue();
    }

    private void expireOAuthStateCookie(HttpServletResponse response) {
        ResponseCookie expiredCookie = ResponseCookie.from(OAUTH_STATE_COOKIE_NAME, "")
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }
}
