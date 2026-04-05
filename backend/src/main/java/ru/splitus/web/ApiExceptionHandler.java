package ru.splitus.web;

import java.time.OffsetDateTime;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiErrorResponse;
import ru.splitus.error.ApiException;

/**
 * Represents api exception handler.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles api exception.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new ApiErrorResponse(exception.getCode().name(), exception.getMessage(), OffsetDateTime.now()));
    }

    /**
     * Handles validation exception.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldErrors().isEmpty()
                ? null
                : exception.getBindingResult().getFieldErrors().get(0);
        String message = fieldError == null ? "Validation failed" : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.VALIDATION_ERROR.name(), message, OffsetDateTime.now()));
    }

    /**
     * Handles constraint violation.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(ApiErrorCode.VALIDATION_ERROR.name(), exception.getMessage(), OffsetDateTime.now()));
    }

    /**
     * Handles generic.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("INTERNAL_ERROR", exception.getMessage(), OffsetDateTime.now()));
    }
}



