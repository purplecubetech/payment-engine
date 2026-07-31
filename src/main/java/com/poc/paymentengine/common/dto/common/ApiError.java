package com.poc.paymentengine.common.dto.common;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(

        boolean success,

        String message,

        String errorCode,

        List<String> errors,

        LocalDateTime timestamp
) {

    public static ApiError of(
            String message,
            String errorCode) {

        return new ApiError(
                false,
                message,
                errorCode,
                List.of(),
                LocalDateTime.now()
        );
    }
}