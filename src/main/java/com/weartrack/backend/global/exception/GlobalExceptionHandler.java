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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        BaseErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage(), null));
    }

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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e) {
        GlobalErrorCode errorCode = GlobalErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllException(Exception e) {
        log.error("Unhandled Exception: ", e);

        GlobalErrorCode errorCode = GlobalErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage(), null));
    }
}
