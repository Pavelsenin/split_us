package ru.splitus.error;

import org.springframework.http.HttpStatus;

/**
 * Represents api failures.
 */
public class ApiException extends RuntimeException {

    private final ApiErrorCode code;
    private final HttpStatus status;

    /**
     * Creates a new api exception instance.
     */
    public ApiException(ApiErrorCode code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    /**
     * Returns the code.
     */
    public ApiErrorCode getCode() {
        return code;
    }

    /**
     * Returns the status.
     */
    public HttpStatus getStatus() {
        return status;
    }
}




