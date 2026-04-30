package com.weartrack.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.dto.request.SocialLoginReqDto;
import com.weartrack.backend.domain.member.dto.response.SocialLoginResDto;
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

    private AuthService authService;

    @BeforeEach
    void setUp() {
        given(socialLoginProviderClient.supports()).willReturn(AuthProvider.KAKAO);
        authService = new AuthService(
                List.of(socialLoginProviderClient),
                authLoginTransactionService
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
                new SocialLoginReqDto(AuthProvider.KAKAO, "auth-code", "oauth-state")
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
                new SocialLoginReqDto(AuthProvider.KAKAO, "new-auth-code", "oauth-state")
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
        SocialLoginReqDto request = new SocialLoginReqDto(AuthProvider.GOOGLE, "auth-code", "oauth-state");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(GeneralException.class);

        verify(socialLoginProviderClient, never()).getUserInfo(any(), any());
        verify(authLoginTransactionService, never()).loginOrRegister(any());
    }
}
