package com.weartrack.backend.domain.member.controller;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.request.SocialLoginReqDto;
import com.weartrack.backend.domain.member.dto.response.SocialLoginResDto;
import com.weartrack.backend.domain.member.service.AuthService;
import com.weartrack.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/social/login")
    /**
     * 프론트가 전달한 소셜 authorization code를 기반으로 자체 JWT를 발급합니다.
     */
    public ApiResponse<SocialLoginResDto> socialLogin(@Valid @RequestBody SocialLoginReqDto request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/login/oauth2/code/google")
    /**
     * Google 로그인 완료 후 전달된 인가 코드를 바로 처리합니다.
     */
    public ApiResponse<SocialLoginResDto> googleCallback(@RequestParam("code") String code) {
        return ApiResponse.success(authService.login(AuthProvider.GOOGLE, code, null));
    }

    @GetMapping("/login/oauth2/code/kakao")
    /**
     * Kakao 로그인 완료 후 전달된 인가 코드를 바로 처리합니다.
     */
    public ApiResponse<SocialLoginResDto> kakaoCallback(@RequestParam("code") String code) {
        return ApiResponse.success(authService.login(AuthProvider.KAKAO, code, null));
    }

    @GetMapping("/login/oauth2/code/naver")
    /**
     * Naver 로그인 완료 후 전달된 인가 코드와 state를 바로 처리합니다.
     */
    public ApiResponse<SocialLoginResDto> naverCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state
    ) {
        return ApiResponse.success(authService.login(AuthProvider.NAVER, code, state));
    }
}
