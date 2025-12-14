package com.dang.identityservice.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private int code;
    private String message;
    private T result;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().code(0).message("Success").result(data).build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder().code(code).message(message).result(null).build();
    }
}
