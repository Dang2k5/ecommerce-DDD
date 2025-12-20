package com.dang.customerservice.interfaces;

import com.dang.customerservice.application.exceptions.*;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(CustomerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> conflict(CustomerAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem(HttpStatus.CONFLICT, ex.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ProblemDetail> badRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problem(HttpStatus.BAD_REQUEST, "Validation failed"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> denied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem(HttpStatus.FORBIDDEN, "Forbidden"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> unknown(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setDetail(detail);
        return pd;
    }
}