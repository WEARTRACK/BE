package com.weartrack.backend.domain.member.exception;

import com.weartrack.backend.global.exception.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_400_1", "지원하지 않는 소셜 로그인 제공자입니다."),
    INVALID_OAUTH_STATE(HttpStatus.BAD_REQUEST, "AUTH_400_2", "유효하지 않은 OAuth state입니다."),
    INVALID_SOCIAL_LOGIN_REQUEST(HttpStatus.BAD_REQUEST, "AUTH_400_3", "유효하지 않은 소셜 로그인 요청입니다."),
    INVALID_OAUTH_HANDOFF(HttpStatus.BAD_REQUEST, "AUTH_400_4", "유효하지 않은 OAuth handoff 토큰입니다."),
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "유효하지 않은 소셜 인증 토큰입니다."),
    SOCIAL_USER_INFO_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "소셜 사용자 정보를 조회하지 못했습니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_3", "유효하지 않은 JWT 토큰입니다."),
    SOCIAL_PROVIDER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AUTH_503_1", "소셜 로그인 제공자를 일시적으로 사용할 수 없습니다."),
    WITHDRAWN_MEMBER(HttpStatus.FORBIDDEN, "AUTH_403_1", "탈퇴한 회원입니다."),
    REJOIN_BLOCKED(HttpStatus.FORBIDDEN, "AUTH_403_2", "탈퇴 후 7일 동안 같은 계정으로 재가입할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
