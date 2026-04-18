package com.weartrack.backend.domain.member.exception;

import com.weartrack.backend.global.exception.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_400_1", "Unsupported social provider."),
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_1", "Invalid social access token."),
    SOCIAL_USER_INFO_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_401_2", "Failed to load social user information."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_3", "Invalid JWT token.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
