package com.weartrack.backend.global.exception;

import com.weartrack.backend.global.exception.code.BaseErrorCode;
import lombok.Getter;

@Getter
/**
 * 공통 에러 코드를 담아 전달하는 예외다.
 */
public class GeneralException extends RuntimeException {

    private final BaseErrorCode errorCode;

    /**
     * 에러 코드를 기반으로 예외를 만든다.
     */
    public GeneralException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
