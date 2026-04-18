package com.weartrack.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.weartrack.backend.domain.member.constant.AuthProvider;
import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.dto.request.SocialLoginReqDto;
import com.weartrack.backend.domain.member.dto.response.SocialLoginResDto;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.entity.SocialAccount;
import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.domain.member.repository.SocialAccountRepository;
import com.weartrack.backend.global.security.JwtTokenProvider;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SocialLoginProviderClient socialLoginProviderClient;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        given(socialLoginProviderClient.supports()).willReturn(AuthProvider.KAKAO);
        authService = new AuthService(
                List.of(socialLoginProviderClient),
                memberRepository,
                socialAccountRepository,
                jwtTokenProvider
        );
    }

    @Test
    @DisplayName("기존 소셜 계정이면 신규 회원 생성 없이 JWT를 발급한다.")
    void loginWithExistingSocialAccount() {
        Member member = Member.createPendingProfile();
        ReflectionTestUtils.setField(member, "memberId", 1L);

        SocialAccount socialAccount = SocialAccount.of(
                member,
                AuthProvider.KAKAO,
                "provider-user-id",
                "weartrack@example.com"
        );

        SocialUserInfo socialUserInfo = new SocialUserInfo(
                AuthProvider.KAKAO,
                "provider-user-id",
                "weartrack@example.com"
        );

        given(socialLoginProviderClient.getUserInfo("auth-code", null)).willReturn(socialUserInfo);
        given(socialAccountRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "provider-user-id"))
                .willReturn(Optional.of(socialAccount));
        given(jwtTokenProvider.createAccessToken(1L)).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("refresh-token");

        SocialLoginResDto response = authService.login(
                new SocialLoginReqDto(AuthProvider.KAKAO, "auth-code", null)
        );

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.profileCompleted()).isFalse();

        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("최초 소셜 로그인 사용자는 member와 social_account를 생성하고 JWT를 발급한다.")
    void loginWithNewSocialAccount() {
        Member savedMember = Member.createPendingProfile();
        ReflectionTestUtils.setField(savedMember, "memberId", 2L);

        SocialUserInfo socialUserInfo = new SocialUserInfo(
                AuthProvider.KAKAO,
                "new-provider-user-id",
                "new-user@example.com"
        );

        given(socialLoginProviderClient.getUserInfo("new-auth-code", null)).willReturn(socialUserInfo);
        given(socialAccountRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "new-provider-user-id"))
                .willReturn(Optional.empty());
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(jwtTokenProvider.createAccessToken(2L)).willReturn("new-access-token");
        given(jwtTokenProvider.createRefreshToken(2L)).willReturn("new-refresh-token");

        SocialLoginResDto response = authService.login(
                new SocialLoginReqDto(AuthProvider.KAKAO, "new-auth-code", null)
        );

        assertThat(response.memberId()).isEqualTo(2L);
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.profileCompleted()).isFalse();

        verify(memberRepository).save(any(Member.class));
        verify(socialAccountRepository).save(any(SocialAccount.class));
    }
}
