package com.dang.orderservice.interfaces.advice;

import com.dang.orderservice.application.exceptions.BadRequestException;
import com.dang.orderservice.domain.model.exception.NotFoundException;
import com.dang.orderservice.application.exceptions.SagaFailedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(err(404, ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> badRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(err(400, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(err(400, "Validation failed"));
    }

    /**
     * Saga fail chủ yếu xảy ra ở listener thread.
     * Bạn có thể đổi thành log-only + DLQ strategy.
     */
    @ExceptionHandler(SagaFailedException.class)
    public ResponseEntity<?> sagaFailed(SagaFailedException ex) {
        return ResponseEntity.status(409).body(err(409, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unknown(Exception ex) {
        return ResponseEntity.status(500).body(err(500, ex.getMessage()));
    }

    private Map<String, Object> err(int code, String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("timestamp", Instant.now().toString());
        m.put("status", code);
        m.put("error", msg);
        return m;
    }
}
