package com.weartrack.backend.global.exception;

import com.weartrack.backend.global.exception.code.BaseErrorCode;
import com.weartrack.backend.global.exception.code.GlobalErrorCode;
import com.weartrack.backend.global.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 애플리케이션 전역 예외를 공통 API 응답으로 변환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외를 표준 실패 응답으로 변환한다.
     */
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        BaseErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage(), null));
    }

    /**
     * 요청 바디 검증 실패를 처리한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("잘못된 요청입니다.");

        GlobalErrorCode errorCode = GlobalErrorCode.BAD_REQUEST;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorMessage, null));
    }

    /**
     * 요청 파라미터 검증 실패를 처리한다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("잘못된 요청입니다.");

        GlobalErrorCode errorCode = GlobalErrorCode.BAD_REQUEST;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorMessage, null));
    }

    /**
     * 경로 변수와 요청 파라미터의 타입 변환 실패를 처리한다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ) {
        GlobalErrorCode errorCode = GlobalErrorCode.BAD_REQUEST;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(
                        errorCode.getCode(),
                        "요청 값의 형식이 올바르지 않습니다.",
                        null
                ));
    }

    /**
     * 지원하지 않는 HTTP 메서드 요청을 처리한다.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e) {
        GlobalErrorCode errorCode = GlobalErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage(), null));
    }

    /**
     * 존재하지 않는 경로(매핑된 핸들러·정적 리소스 없음) 요청을 404 응답으로 변환한다.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.debug("리소스를 찾을 수 없습니다.: {}", e.getResourcePath());

        GlobalErrorCode errorCode = GlobalErrorCode.NOT_FOUND;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage(), null));
    }

    /**
     * 처리되지 않은 예외를 공통 서버 에러 응답으로 변환한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllException(Exception e) {
        log.error("처리되지 않은 예외: ", e);

        GlobalErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage(), null));
    }
}
