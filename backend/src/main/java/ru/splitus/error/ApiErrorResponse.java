package ru.splitus.error;

import java.time.OffsetDateTime;

/**
 * Represents the api error response payload.
 */
public class ApiErrorResponse {

    private final String code;
    private final String message;
    private final OffsetDateTime timestamp;

    /**
     * Creates a new api error response instance.
     */
    public ApiErrorResponse(String code, String message, OffsetDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * Returns the code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the timestamp.
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}




