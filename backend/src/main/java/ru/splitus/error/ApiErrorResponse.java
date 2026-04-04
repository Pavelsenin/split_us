package ru.splitus.error;

import java.time.OffsetDateTime;

public class ApiErrorResponse {

    private final String code;
    private final String message;
    private final OffsetDateTime timestamp;

    public ApiErrorResponse(String code, String message, OffsetDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}

