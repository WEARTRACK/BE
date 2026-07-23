package com.weartrack.backend.domain.member.service;

import com.weartrack.backend.domain.member.dto.response.TokenRefreshResDto;
import com.weartrack.backend.domain.member.entity.Member;
import com.weartrack.backend.domain.member.entity.RefreshToken;
import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.domain.member.repository.MemberRepository;
import com.weartrack.backend.domain.member.repository.RefreshTokenRepository;
import com.weartrack.backend.global.exception.GeneralException;
import com.weartrack.backend.global.security.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    public RefreshTokenService(
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenRepository refreshTokenRepository,
            MemberRepository memberRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public String issue(Member member) {
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getMemberId());
        refreshTokenRepository.save(RefreshToken.issue(
                member,
                hash(refreshToken),
                LocalDateTime.ofInstant(jwtTokenProvider.extractExpiresAtFromRefreshToken(refreshToken), SYSTEM_ZONE)
        ));
        return refreshToken;
    }

    @Transactional
    public TokenRefreshResDto rotate(String rawRefreshToken) {
        Long memberId = jwtTokenProvider.extractMemberIdFromRefreshToken(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new GeneralException(AuthErrorCode.INVALID_JWT_TOKEN));

        LocalDateTime now = LocalDateTime.now();
        if (!refreshToken.belongsTo(memberId) || !refreshToken.isActive(now)) {
            throw new GeneralException(AuthErrorCode.INVALID_JWT_TOKEN);
        }

        Member member = refreshToken.getMember();
        if (!memberRepository.existsByMemberIdAndDeletedAtIsNull(memberId)) {
            refreshToken.revoke(now);
            throw new GeneralException(AuthErrorCode.INVALID_JWT_TOKEN);
        }

        refreshToken.rotate(now);
        String newRefreshToken = issue(member);

        return new TokenRefreshResDto(
                jwtTokenProvider.createAccessToken(memberId),
                newRefreshToken
        );
    }

    private String hash(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] digest = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }
}
