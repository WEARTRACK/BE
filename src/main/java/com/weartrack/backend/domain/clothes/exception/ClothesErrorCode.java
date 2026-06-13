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
    ),

    PRODUCT_LINK_INVALID_URL(
            "PRODUCT_LINK_4001",
            "잘못된 상품 URL입니다.",
            HttpStatus.BAD_REQUEST
    ),

    PRODUCT_LINK_UNSUPPORTED_URL(
            "PRODUCT_LINK_4002",
            "지원하지 않는 URL 형식입니다.",
            HttpStatus.BAD_REQUEST
    ),

    PRODUCT_LINK_FETCH_FAILED(
            "PRODUCT_LINK_4003",
            "상품 정보를 불러올 수 없습니다. 상품 페이지를 확인하거나 직접 입력해 주세요.",
            HttpStatus.BAD_REQUEST
    ),

    PRODUCT_LINK_PARSE_FAILED(
            "PRODUCT_LINK_4004",
            "상품 정보를 불러올 수 없습니다. 상품 페이지를 확인하거나 직접 입력해 주세요.",
            HttpStatus.BAD_REQUEST
    ),

    PRODUCT_LINK_DUPLICATED(
            "PRODUCT_LINK_4005",
            "이미 등록된 상품 링크입니다.",
            HttpStatus.CONFLICT
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
}
