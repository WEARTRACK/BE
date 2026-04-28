package com.weartrack.backend.global.exception.code;

import org.springframework.http.HttpStatus;

/**
 * 공통 에러 코드 인터페이스다.
 */
public interface BaseErrorCode {
    String getCode();
    String getMessage();
    HttpStatus getStatus();
}
