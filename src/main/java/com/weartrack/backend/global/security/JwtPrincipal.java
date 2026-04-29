package com.weartrack.backend.global.security;

/**
 * 인증된 회원 식별자를 담는 principal이다.
 */
public record JwtPrincipal(Long memberId) {
}
