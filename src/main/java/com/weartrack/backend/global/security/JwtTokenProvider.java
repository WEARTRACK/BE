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
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("memberId", Long.class);
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
