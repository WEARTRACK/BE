package com.weartrack.backend.domain.member.exception;

import com.weartrack.backend.global.exception.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404_1", "사용자를 찾을 수 없습니다."),
    MEMBER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "MEMBER_400_1", "이미 탈퇴한 회원입니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "MEMBER_400_2", "필수 약관에 동의해야 합니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_409_1", "이미 사용 중인 닉네임입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
