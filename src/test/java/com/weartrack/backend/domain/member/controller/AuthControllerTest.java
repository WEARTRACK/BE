package com.weartrack.backend.domain.member.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weartrack.backend.domain.member.constant.AuthClientType;
import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.OAuthStatePayload;
import com.weartrack.backend.domain.member.dto.request.SocialLoginReqDto;
import com.weartrack.backend.domain.member.dto.response.SocialLoginResDto;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.domain.member.service.AuthService;
import com.weartrack.backend.domain.member.service.OAuthHandoffService;
import com.weartrack.backend.domain.member.service.OAuthStateService;
import com.weartrack.backend.domain.member.service.SocialOAuthAuthorizeService;
import com.weartrack.backend.global.exception.GeneralException;
import com.weartrack.backend.global.exception.GlobalExceptionHandler;
import com.weartrack.backend.global.security.JwtAuthenticationFilter;
import com.weartrack.backend.global.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private OAuthHandoffService oAuthHandoffService;

    @MockitoBean
    private OAuthStateService oAuthStateService;

    @MockitoBean
    private SocialOAuthAuthorizeService socialOAuthAuthorizeService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("OAuth 시작 요청이면 provider authorize URL로 리다이렉트한다.")
    void authorizeRedirectsToProvider() throws Exception {
        given(socialOAuthAuthorizeService.buildAuthorizeUri(AuthProvider.KAKAO, AuthClientType.MOBILE))
                .willReturn(URI.create("https://kauth.kakao.com/oauth/authorize?client_id=test"));

        mockMvc.perform(get("/api/auth/social/authorize/kakao"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://kauth.kakao.com/oauth/authorize?client_id=test"));
    }

    @Test
    @DisplayName("소셜 로그인 요청이 유효하면 ApiResponse 형식으로 응답한다.")
    void socialLoginSuccess() throws Exception {
        SocialLoginReqDto request = new SocialLoginReqDto(AuthProvider.KAKAO, "auth-code", "oauth-state", null);
        SocialLoginResDto response = new SocialLoginResDto(
                1L,
                null,
                false,
                "access-token",
                "refresh-token"
        );

        given(authService.login(request)).willReturn(response);

        mockMvc.perform(post("/api/auth/social/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.memberId").value(1L))
                .andExpect(jsonPath("$.result.accessToken").value("access-token"))
                .andExpect(jsonPath("$.result.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("소셜 로그인 요청에서 authorizationCode가 비어 있으면 검증 오류를 반환한다.")
    void socialLoginValidationFail() throws Exception {
        SocialLoginReqDto request = new SocialLoginReqDto(AuthProvider.KAKAO, null, "oauth-state", null);
        given(authService.login(request)).willThrow(new GeneralException(AuthErrorCode.INVALID_SOCIAL_LOGIN_REQUEST));

        mockMvc.perform(post("/api/auth/social/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }

    @Test
    @DisplayName("카카오 callback 요청이 들어오면 provider와 code를 서비스로 전달한다.")
    void kakaoCallbackSuccess() throws Exception {
        SocialLoginResDto response = new SocialLoginResDto(
                1L,
                null,
                false,
                "access-token",
                "refresh-token"
        );

        given(authService.login(eq(AuthProvider.KAKAO), eq("kakao-code"), eq("kakao-state"), eq("kakao-state")))
                .willReturn(response);
        given(oAuthStateService.consumeIfPresent(AuthProvider.KAKAO, "kakao-state"))
                .willReturn(java.util.Optional.empty());

        mockMvc.perform(get("/login/oauth2/code/kakao")
                        .cookie(new Cookie("oauth-login-state", "kakao-state"))
                        .param("state", "kakao-state")
                        .param("code", "kakao-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("모바일 callback 요청이면 앱 딥링크로 handoff token을 리다이렉트한다.")
    void kakaoMobileCallbackRedirectsToApp() throws Exception {
        given(oAuthStateService.consumeIfPresent(AuthProvider.KAKAO, "mobile-oauth-state"))
                .willReturn(java.util.Optional.of(new OAuthStatePayload(AuthProvider.KAKAO, AuthClientType.MOBILE)));
        given(oAuthHandoffService.create(AuthProvider.KAKAO, "kakao-code", "mobile-oauth-state"))
                .willReturn("handoff-token");

        mockMvc.perform(get("/login/oauth2/code/kakao")
                        .param("state", "mobile-oauth-state")
                        .param("code", "kakao-code"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("weartrack://auth/callback/kakao?handoff=handoff-token"));
    }

    @Test
    @DisplayName("네이버 callback 요청은 state를 함께 서비스로 전달한다.")
    void naverCallbackSuccess() throws Exception {
        SocialLoginResDto response = new SocialLoginResDto(
                2L,
                null,
                false,
                "naver-access-token",
                "naver-refresh-token"
        );

        given(authService.login(eq(AuthProvider.NAVER), eq("naver-code"), eq("naver-state"), eq("naver-state")))
                .willReturn(response);
        given(oAuthStateService.consumeIfPresent(AuthProvider.NAVER, "naver-state"))
                .willReturn(java.util.Optional.empty());

        mockMvc.perform(get("/login/oauth2/code/naver")
                        .cookie(new Cookie("oauth-login-state", "naver-state"))
                        .param("code", "naver-code")
                        .param("state", "naver-state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.memberId").value(2L));
    }
}
