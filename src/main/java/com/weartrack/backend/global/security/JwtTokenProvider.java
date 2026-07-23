package com.weartrack.backend.global.security;

import com.weartrack.backend.domain.member.exception.AuthErrorCode;
import com.weartrack.backend.global.exception.GeneralException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 생성과 파싱을 담당한다.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtTokenProvider(
            @Value("${JWT_SECRET}") String secret,
            @Value("${JWT_ACCESS_TOKEN_EXPIRATION_SECONDS:3600}") long accessTokenExpirationSeconds,
            @Value("${JWT_REFRESH_TOKEN_EXPIRATION_SECONDS:1209600}") long refreshTokenExpirationSeconds
    ) {
        this.secretKey = createSecretKey(secret);
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    /**
     * 서비스 내부 인증에 사용할 access token을 생성합니다.
     */
    public String createAccessToken(Long memberId) {
        return createToken(memberId, accessTokenExpirationSeconds, "access");
    }

    /**
     * 토큰 재발급에 사용할 refresh token을 생성합니다.
     */
    public String createRefreshToken(Long memberId) {
        return createToken(memberId, refreshTokenExpirationSeconds, "refresh");
    }

    /**
     * 서명 검증이 끝난 JWT에서 memberId claim을 추출합니다.
     */
    public Long extractMemberId(String token) {
        return extractMemberId(token, "access");
    }

    public Long extractMemberIdFromRefreshToken(String token) {
        return extractMemberId(token, "refresh");
    }

    public Instant extractExpiresAtFromRefreshToken(String token) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get("tokenType", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new GeneralException(AuthErrorCode.INVALID_JWT_TOKEN);
        }
        return claims.getExpiration().toInstant();
    }

    private Long extractMemberId(String token, String expectedTokenType) {
        Claims claims = parseClaims(token);
        String tokenType = claims.get("tokenType", String.class);
        if (!expectedTokenType.equals(tokenType)) {
            throw new GeneralException(AuthErrorCode.INVALID_JWT_TOKEN);
        }

        return claims.get("memberId", Long.class);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new GeneralException(AuthErrorCode.INVALID_JWT_TOKEN);
        }
    }

    /**
     * 공통 JWT claim과 만료 시간을 설정해 access 또는 refresh token을 생성합니다.
     */
    private String createToken(Long memberId, long expirationSeconds, String tokenType) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("memberId", memberId)
                .claim("tokenType", tokenType)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 운영 환경에서는 base64 secret을, 로컬에서는 일반 문자열 secret을 모두 허용합니다.
     */
    private SecretKey createSecretKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (IllegalArgumentException | DecodingException e) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
}
