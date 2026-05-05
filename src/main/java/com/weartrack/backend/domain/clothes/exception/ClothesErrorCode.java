package com.weartrack.backend.domain.clothes.exception;

import com.weartrack.backend.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClothesErrorCode implements BaseErrorCode {

    CLOTHES_NOT_FOUND("CLOTHES_4001", "해당 옷의 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    CLOTHES_NOT_OWNED("CLOTHES_4002", "본인의 옷이 아닙니다.", HttpStatus.FORBIDDEN)
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
