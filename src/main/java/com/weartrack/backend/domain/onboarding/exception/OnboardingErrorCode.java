package com.weartrack.backend.domain.onboarding.exception;

import com.weartrack.backend.global.exception.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OnboardingErrorCode implements BaseErrorCode {

    ONBOARDING_NOT_FOUND(
            "ONBOARDING_4001",
            "온보딩 정보를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    ),
    QUEST_NOT_FOUND(
            "ONBOARDING_4002",
            "온보딩 퀘스트 정보를 찾을 수 없습니다.",
            HttpStatus.NOT_FOUND
    );

    private final String code;
    private final String message;
    private final HttpStatus status;
}