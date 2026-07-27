package com.weartrack.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.weartrack.backend.domain.member.dto.response.TokenRefreshResDto;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.entity.RefreshToken;
import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.domain.member.repository.RefreshTokenRepository;
import com.weartrack.backend.global.security.JwtTokenProvider;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private MemberRepository memberRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(jwtTokenProvider, refreshTokenRepository, memberRepository);
    }

    @Test
    @DisplayName("refresh token 발급 시 원문 대신 hash를 저장한다.")
    void issueStoresTokenHash() {
        Member member = createMember(1L);
        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("raw-refresh-token");
        given(jwtTokenProvider.extractExpiresAtFromRefreshToken("raw-refresh-token"))
                .willReturn(Instant.now().plusSeconds(1209600));

        String refreshToken = refreshTokenService.issue(member);

        assertThat(refreshToken).isEqualTo("raw-refresh-token");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isNotEqualTo("raw-refresh-token");
        assertThat(captor.getValue().getTokenHash()).hasSize(64);
    }

    @Test
    @DisplayName("refresh token 재발급 시 기존 token을 폐기하고 새 token을 저장한다.")
    void rotateRevokesOldTokenAndIssuesNewToken() {
        Member member = createMember(1L);
        RefreshToken oldRefreshToken = RefreshToken.issue(
                member,
                "old-token-hash",
                LocalDateTime.now().plusDays(1)
        );

        given(jwtTokenProvider.extractMemberIdFromRefreshToken("old-refresh-token")).willReturn(1L);
        given(refreshTokenRepository.findByTokenHash(anyString())).willReturn(Optional.of(oldRefreshToken));
        given(memberRepository.existsByMemberIdAndDeletedAtIsNull(1L)).willReturn(true);
        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("new-refresh-token");
        given(jwtTokenProvider.extractExpiresAtFromRefreshToken("new-refresh-token"))
                .willReturn(Instant.now().plusSeconds(1209600));
        given(jwtTokenProvider.createAccessToken(1L)).willReturn("new-access-token");

        TokenRefreshResDto response = refreshTokenService.rotate("old-refresh-token");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(oldRefreshToken.getRevokedAt()).isNotNull();
        assertThat(oldRefreshToken.getRotatedAt()).isNotNull();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    private Member createMember(Long memberId) {
        Member member = Member.createPendingProfile();
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;
    }
}
