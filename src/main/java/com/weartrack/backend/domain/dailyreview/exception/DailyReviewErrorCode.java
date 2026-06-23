package com.weartrack.backend.domain.dailyreview.exception;

import com.weartrack.backend.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DailyReviewErrorCode implements BaseErrorCode {

    INVALID_CLOTHES_SELECTION("DAILY_REVIEW_4001", "본인의 옷만 선택할 수 있습니다.", HttpStatus.BAD_REQUEST),
    DAILY_REVIEW_ALREADY_EXISTS("DAILY_REVIEW_4091", "이미 오늘 입은 옷 기록이 저장되었습니다.", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
