package com.dang.identityservice.interfaces.rest;

import com.dang.identityservice.application.dto.ApiResponse;
import com.dang.identityservice.application.exception.AppException;
import com.dang.identityservice.application.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleApp(AppException ex) {
        ErrorCode code = ex.getErrorCode();
        return ResponseEntity.status(mapStatus(code))
                .body(ApiResponse.error(code.getCode(), ex.getMessage()));
    }

    /**
     * Map DB unique constraint violations (and similar integrity errors) into domain-friendly errors.
     * This is still needed even when we proactively check existsBy* because of race conditions.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = rootMessage(ex);
        String lower = msg.toLowerCase();

        ErrorCode code;
        String outMsg;

        if (lower.contains("users") && lower.contains("username")) {
            code = ErrorCode.USER_EXISTED;
            outMsg = code.getMessage();
        } else if (lower.contains("users") && lower.contains("email")) {
            code = ErrorCode.USER_EMAIL_EXISTED;
            outMsg = code.getMessage();
        } else if (lower.contains("roles") && lower.contains("code")) {
            code = ErrorCode.ROLE_EXISTED;
            outMsg = code.getMessage();
        } else if (lower.contains("permissions") && lower.contains("code")) {
            code = ErrorCode.PERMISSION_EXISTED;
            outMsg = code.getMessage();
        } else {
            code = ErrorCode.INVALID_REQUEST;
            outMsg = "Data integrity violation";
        }

        return ResponseEntity.badRequest().body(ApiResponse.error(code.getCode(), outMsg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        // Lấy lỗi field đầu tiên cho gọn (bạn có thể mở rộng trả list)
        String msg = ex.getBindingResult().getFieldErrors().isEmpty()
                ? ErrorCode.INVALID_REQUEST.getMessage()
                : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(),
                        ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()));
    }

    private static HttpStatus mapStatus(ErrorCode code) {
        return switch (code) {
            case UNAUTHENTICATED,
                 TOKEN_INVALID,
                 TOKEN_REVOKED,
                 REFRESH_TOKEN_INVALID,
                 REFRESH_TOKEN_EXPIRED,
                 REFRESH_TOKEN_REVOKED -> HttpStatus.UNAUTHORIZED;
            case UNAUTHORIZED -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private static String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur.getMessage() == null ? "" : cur.getMessage();
    }
}
