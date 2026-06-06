package com.weartrack.backend.domain.clothes.exception;

import com.weartrack.backend.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClothesErrorCode implements BaseErrorCode {

    CLOTHES_NOT_FOUND(
            "CLOTHES_4001",
            "해당 옷의 정보를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),

    CLOTHES_NOT_OWNED(
            "CLOTHES_4002",
            "본인의 옷이 아닙니다.",
            HttpStatus.FORBIDDEN
    ),

    CLOTHES_PHOTO_NOT_FOUND(
            "CLOTHES_PHOTO_4001",
            "해당 옷 사진 정보를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),

    CLOTHES_PHOTO_NOT_OWNED(
            "CLOTHES_PHOTO_4002",
            "본인의 옷 사진이 아닙니다.",
            HttpStatus.FORBIDDEN
    ),

    CLOTHES_IMAGE_READ_FAILED(
            "CLOTHES_PHOTO_4003",
            "이미지 파일을 읽는 중 오류가 발생했습니다.",
            HttpStatus.BAD_REQUEST
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
}