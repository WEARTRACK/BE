package com.weartrack.backend.domain.closet.exception;

import com.weartrack.backend.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ClosetErrorCode implements BaseErrorCode {

    INVALID_TEMPLATE_ID("CLOSET_4001", "존재하지 않는 옷장 템플릿입니다.", HttpStatus.BAD_REQUEST),
    INVALID_SECTION_COUNT("CLOSET_4002", "템플릿의 칸 개수와 요청 칸 개수가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    EMPTY_SECTION_NAME("CLOSET_4003", "칸 이름을 모두 입력해주세요.", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE("CLOSET_4004", "옷장 이미지는 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_SECTION_ORDER("CLOSET_4005", "템플릿의 칸 순서가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    CLOSET_NOT_FOUND("CLOSET_4006", "등록한 옷장을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CLOSET_NOT_OWNED("CLOSET_4007", "사용자의 옷장이 아닙니다.", HttpStatus.FORBIDDEN),
    SECTION_FULL("CLOSET_4008", "칸의 자리가 없습니다.", HttpStatus.BAD_REQUEST),
    SECTION_NOT_FOUND("CLOSET_4009","해당 칸을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SECTION_NOT_IN_CLOSET("CLOSET_4010", "해당 옷장에 속하지 않는 섹션입니다.", HttpStatus.BAD_REQUEST),
    SECTION_COUNT_MISMATCH("CLOSET_4011", "템플릿의 칸 개수가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    SECTION_NOT_OWNED("CLOSET_4012", "본인의 섹션이 아닙니다.", HttpStatus.FORBIDDEN),
    DUPLICATE_CLOSET("CLOSET_4013", "이미 등록된 옷장이 있습니다.", HttpStatus.CONFLICT),
    CLOSET_IMAGE_SAVE_FAILED("CLOSET_5001", "옷장 이미지 저장 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}