package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.dto.SocialUserInfo;
import com.weartrack.backend.domain.member.dto.response.SocialLoginResDto;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.entity.SocialAccount;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.domain.member.repository.SocialAccountRepository;
import com.weartrack.backend.global.exception.GeneralException;
import com.weartrack.backend.global.security.JwtTokenProvider;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthLoginTransactionService {

    private static final long REJOIN_BLOCK_DAYS = 7;

    private final SocialAccountRepository socialAccountRepository;
    private final AuthRegistrationService authRegistrationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthLoginTransactionService(
            SocialAccountRepository socialAccountRepository,
            AuthRegistrationService authRegistrationService,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenService refreshTokenService
    ) {
        this.socialAccountRepository = socialAccountRepository;
        this.authRegistrationService = authRegistrationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public SocialLoginResDto loginOrRegister(SocialUserInfo socialUserInfo) {
        Member member = socialAccountRepository
                .findByProviderAndProviderUserId(socialUserInfo.provider(), socialUserInfo.providerUserId())
                .map(SocialAccount::getMember)
                .orElseGet(() -> findOrRegisterMember(socialUserInfo));

        reactivateIfRejoinAllowed(member);

        String accessToken = jwtTokenProvider.createAccessToken(member.getMemberId());
        String refreshToken = refreshTokenService.issue(member);

        return new SocialLoginResDto(
                member.getMemberId(),
                member.getNickname(),
                member.hasNickname(),
                accessToken,
                refreshToken
        );
    }

    private Member findOrRegisterMember(SocialUserInfo socialUserInfo) {
        try {
            return authRegistrationService.registerNewMember(socialUserInfo);
        } catch (DataIntegrityViolationException e) {
            return socialAccountRepository
                    .findByProviderAndProviderUserId(socialUserInfo.provider(), socialUserInfo.providerUserId())
                    .map(SocialAccount::getMember)
                    .orElseThrow(() -> e);
        }
    }

    private void reactivateIfRejoinAllowed(Member member) {
        if (!member.isWithdrawn()) {
            return;
        }

        if (member.isRejoinBlocked(LocalDateTime.now(), REJOIN_BLOCK_DAYS)) {
            throw new GeneralException(AuthErrorCode.REJOIN_BLOCKED);
        }

        member.reactivate();
    }
}
