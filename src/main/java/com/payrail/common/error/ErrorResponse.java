package com.payrail.common.error;

import java.time.Instant;

public record ErrorResponse(ErrorDetail error)
{

    public record ErrorDetail(ErrorCode code, String message, String requestId, Instant timestamp) {}

    public static ErrorResponse of(ErrorCode code, String message, String requestId)
    {
        return new ErrorResponse(new ErrorDetail(code, message, requestId, Instant.now()));
    }
}