package com.example.projectback.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, null, message);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
