package com.dang.inventoryservice.interfaces;

import com.dang.inventoryservice.domain.model.exception.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> notFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<?> badRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
