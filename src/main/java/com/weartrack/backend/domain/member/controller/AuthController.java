package com.weartrack.backend.domain.member.controller;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.request.SocialLoginReqDto;
import com.weartrack.backend.domain.member.dto.response.SocialLoginResDto;
import com.weartrack.backend.domain.member.service.AuthService;
import com.weartrack.backend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ApiResponse<SocialLoginResDto> googleCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        try {
            SocialLoginResDto response = authService.login(
                    AuthProvider.GOOGLE,
                    code,
                    state,
                    extractOAuthState(httpRequest)
            );
            return ApiResponse.success(response);
        } finally {
            expireOAuthStateCookie(httpResponse);
        }
    }


    /**
     * Kakao 로그인 완료 후 전달된 인가 코드를 바로 처리합니다.
     */
    @GetMapping("/login/oauth2/code/kakao")
    public ApiResponse<SocialLoginResDto> kakaoCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        try {
            SocialLoginResDto response = authService.login(
                    AuthProvider.KAKAO,
                    code,
                    state,
                    extractOAuthState(httpRequest)
            );
            return ApiResponse.success(response);
        } finally {
            expireOAuthStateCookie(httpResponse);
        }
    }


    /**
     * Naver 로그인 완료 후 전달된 인가 코드와 state를 바로 처리합니다.
     */
    @GetMapping("/login/oauth2/code/naver")
    public ApiResponse<SocialLoginResDto> naverCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        try {
            SocialLoginResDto response = authService.login(
                    AuthProvider.NAVER,
                    code,
                    state,
                    extractOAuthState(httpRequest)
            );
            return ApiResponse.success(response);
        } finally {
            expireOAuthStateCookie(httpResponse);
        }
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
