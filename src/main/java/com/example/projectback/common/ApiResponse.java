package com.example.projectback.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;

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
