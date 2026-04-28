package com.weartrack.backend.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * API 공통 응답 형식이다.
 */
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public record ApiResponse<T>(
        boolean isSuccess,
        String code,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        T result
) {
    // 데이터가 있는 성공 응답
    /**
     * 결과 데이터를 포함한 성공 응답을 만든다.
     */
    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(true, "COMMON_200", "요청에 성공했습니다.", result);
    }
    //데이터가 없는 성공 응답 (오버로딩)
    /**
     * 결과 데이터가 없는 성공 응답을 만든다.
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, "COMMON_200", "요청에 성공했습니다.", null);
    }
    // 실패 시 호출
    /**
     * 실패 응답을 만든다.
     */
    public static <T> ApiResponse<T> failure(String code, String message, T result) {
        return new ApiResponse<>(false, code, message, result);
    }
}
