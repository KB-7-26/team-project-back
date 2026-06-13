package com.example.projectback.common.exception;

public record ApiErrorResponse(
        boolean success,
        Object data,
        String message
) {

    public static ApiErrorResponse of(String message) {
        return new ApiErrorResponse(false, null, message);
    }
}
