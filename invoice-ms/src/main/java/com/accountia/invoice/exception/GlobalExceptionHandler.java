package com.accountia.invoice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized error handling for all controllers.
 *
 * <p>Every exception type is mapped to:
 * <ul>
 *   <li>A consistent JSON response shape: {@code {success, message, errors, timestamp}}</li>
 *   <li>The appropriate HTTP status code</li>
 * </ul>
 *
 * <p>{@code @RestControllerAdvice} = {@code @ControllerAdvice} + {@code @ResponseBody}
 * — no need to annotate each method with @ResponseBody.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 404 Not Found ──────────────────────────────────────────────────────────

    @ExceptionHandler({InvoiceNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(false, ex.getMessage(), List.of(), Instant.now()));
    }

    // ── 400 Bad Request ────────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, ex.getMessage(), List.of(), Instant.now()));
    }

    /**
     * Handles @Valid annotation failures on request bodies.
     * Returns field-level error details so the frontend can highlight specific fields.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Collect all field error messages into a list
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fe) {
                        return fe.getField() + ": " + fe.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Validation failed", errors, Instant.now()));
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────────

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(false, ex.getMessage(), List.of(), Instant.now()));
    }

    // ── 413 Payload Too Large ──────────────────────────────────────────────────

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(false, "File too large. Maximum size is 10MB.", List.of(), Instant.now()));
    }

    // ── 500 Internal Server Error ──────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // Log the full stack trace for debugging — never expose it in the response
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, "An unexpected error occurred", List.of(), Instant.now()));
    }

    // ── Response shape ─────────────────────────────────────────────────────────

    /**
     * Consistent error response body.
     * The frontend's {@code ApiError.fromResponse()} reads the {@code message} field.
     */
    public record ErrorResponse(
            boolean success,
            String message,
            List<String> errors,
            Instant timestamp
    ) {}
}
