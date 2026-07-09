package com.weartrack.backend.domain.member.exception;

import com.weartrack.backend.global.exception.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_400_1", "Unsupported social provider."),
    INVALID_OAUTH_STATE(HttpStatus.BAD_REQUEST, "AUTH_400_2", "Invalid OAuth state."),
    INVALID_SOCIAL_LOGIN_REQUEST(HttpStatus.BAD_REQUEST, "AUTH_400_3", "Invalid social login request."),
    INVALID_OAUTH_HANDOFF(HttpStatus.BAD_REQUEST, "AUTH_400_4", "Invalid OAuth handoff token."),
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "Invalid social access token."),
    SOCIAL_USER_INFO_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "Failed to load social user information."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_3", "Invalid JWT token."),
    WITHDRAWN_MEMBER(HttpStatus.FORBIDDEN, "AUTH_403_1", "탈퇴한 회원입니다."),
    REJOIN_BLOCKED(HttpStatus.FORBIDDEN, "AUTH_403_2", "탈퇴 후 7일 동안 같은 계정으로 재가입할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
