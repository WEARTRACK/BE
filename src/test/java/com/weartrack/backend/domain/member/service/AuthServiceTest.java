package com.weartrack.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.OAuthHandoffPayload;
import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.dto.request.SocialLoginReqDto;
import com.weartrack.backend.domain.member.dto.request.TokenRefreshReqDto;
import com.weartrack.backend.domain.member.dto.response.SocialLoginResDto;
import com.weartrack.backend.domain.member.dto.response.TokenRefreshResDto;
import com.weartrack.backend.global.exception.GeneralException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SocialLoginProviderClient socialLoginProviderClient;

    @Mock
    private AuthLoginTransactionService authLoginTransactionService;

    @Mock
    private OAuthHandoffService oAuthHandoffService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        given(socialLoginProviderClient.supports()).willReturn(AuthProvider.KAKAO);
        authService = new AuthService(
                List.of(socialLoginProviderClient),
                authLoginTransactionService,
                oAuthHandoffService,
                refreshTokenService
        );
    }

    @Test
    @DisplayName("기존 소셜 계정이면 외부 조회 후 로그인 트랜잭션을 수행한다.")
    void loginWithExistingSocialAccount() {
        SocialUserInfo socialUserInfo = new SocialUserInfo(
                AuthProvider.KAKAO,
                "provider-user-id",
                "weartrack@example.com"
        );
        SocialLoginResDto expectedResponse = new SocialLoginResDto(
                1L,
                null,
                false,
                "access-token",
                "refresh-token"
        );

        given(socialLoginProviderClient.getUserInfo("auth-code", "oauth-state")).willReturn(socialUserInfo);
        given(authLoginTransactionService.loginOrRegister(socialUserInfo)).willReturn(expectedResponse);

        SocialLoginResDto response = authService.login(
                new SocialLoginReqDto(AuthProvider.KAKAO, "auth-code", "oauth-state", null)
        );

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.profileCompleted()).isFalse();
        verify(authLoginTransactionService).loginOrRegister(socialUserInfo);
    }

    @Test
    @DisplayName("신규 소셜 계정이면 외부 조회 후 가입 트랜잭션 결과를 반환한다.")
    void loginWithNewSocialAccount() {
        SocialUserInfo socialUserInfo = new SocialUserInfo(
                AuthProvider.KAKAO,
                "new-provider-user-id",
                "new-user@example.com"
        );
        SocialLoginResDto expectedResponse = new SocialLoginResDto(
                2L,
                null,
                false,
                "new-access-token",
                "new-refresh-token"
        );

        given(socialLoginProviderClient.getUserInfo("new-auth-code", "oauth-state")).willReturn(socialUserInfo);
        given(authLoginTransactionService.loginOrRegister(socialUserInfo)).willReturn(expectedResponse);

        SocialLoginResDto response = authService.login(
                new SocialLoginReqDto(AuthProvider.KAKAO, "new-auth-code", "oauth-state", null)
        );

        assertThat(response.memberId()).isEqualTo(2L);
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.profileCompleted()).isFalse();
        verify(authLoginTransactionService).loginOrRegister(socialUserInfo);
    }

    @Test
    @DisplayName("state 검증이 필요한 콜백 로그인은 검증 후 트랜잭션을 수행한다.")
    void callbackLoginValidatesState() {
        SocialUserInfo socialUserInfo = new SocialUserInfo(
                AuthProvider.KAKAO,
                "provider-user-id",
                "weartrack@example.com"
        );
        SocialLoginResDto expectedResponse = new SocialLoginResDto(
                3L,
                null,
                false,
                "access-token",
                "refresh-token"
        );

        given(socialLoginProviderClient.getUserInfo("auth-code", "oauth-state")).willReturn(socialUserInfo);
        given(authLoginTransactionService.loginOrRegister(socialUserInfo)).willReturn(expectedResponse);

        SocialLoginResDto response = authService.login(
                AuthProvider.KAKAO,
                "auth-code",
                "oauth-state",
                "oauth-state"
        );

        assertThat(response.memberId()).isEqualTo(3L);
        verify(authLoginTransactionService).loginOrRegister(socialUserInfo);
    }

    @Test
    @DisplayName("지원하지 않는 provider면 외부 호출 전에 예외를 던진다.")
    void loginFailsWhenProviderUnsupported() {
        SocialLoginReqDto request = new SocialLoginReqDto(AuthProvider.GOOGLE, "auth-code", "oauth-state", null);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(GeneralException.class);

        verify(socialLoginProviderClient, never()).getUserInfo(any(), any());
        verify(authLoginTransactionService, never()).loginOrRegister(any());
    }

    @Test
    @DisplayName("handoff token 로그인 요청이면 저장된 code/state로 provider 조회를 진행한다.")
    void loginWithHandoffToken() {
        SocialUserInfo socialUserInfo = new SocialUserInfo(
                AuthProvider.KAKAO,
                "provider-user-id",
                "weartrack@example.com"
        );
        SocialLoginResDto expectedResponse = new SocialLoginResDto(
                4L,
                null,
                false,
                "access-token",
                "refresh-token"
        );

        given(oAuthHandoffService.consume(AuthProvider.KAKAO, "handoff-token"))
                .willReturn(new OAuthHandoffPayload(AuthProvider.KAKAO, "auth-code", "server-state"));
        given(socialLoginProviderClient.getUserInfo("auth-code", "server-state")).willReturn(socialUserInfo);
        given(authLoginTransactionService.loginOrRegister(socialUserInfo)).willReturn(expectedResponse);

        SocialLoginResDto response = authService.login(
                new SocialLoginReqDto(AuthProvider.KAKAO, null, null, "handoff-token")
        );

        assertThat(response.memberId()).isEqualTo(4L);
        verify(oAuthHandoffService).consume(AuthProvider.KAKAO, "handoff-token");
        verify(authLoginTransactionService).loginOrRegister(socialUserInfo);
    }

    @Test
    @DisplayName("refresh token이 유효하면 access token과 refresh token을 재발급한다.")
    void refreshTokenSuccess() {
        given(refreshTokenService.rotate("refresh-token"))
                .willReturn(new TokenRefreshResDto("new-access-token", "new-refresh-token"));

        TokenRefreshResDto response = authService.refresh(new TokenRefreshReqDto("refresh-token"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("refresh token의 회원이 없거나 탈퇴 상태면 재발급하지 않는다.")
    void refreshTokenFailsWhenMemberInactive() {
        given(refreshTokenService.rotate("refresh-token")).willThrow(new GeneralException(
                com.weartrack.backend.domain.member.exception.AuthErrorCode.INVALID_JWT_TOKEN
        ));

        assertThatThrownBy(() -> authService.refresh(new TokenRefreshReqDto("refresh-token")))
                .isInstanceOf(GeneralException.class);
    }
}
