package com.example.securedocviewer.controller;

import com.example.securedocviewer.exception.BadRequestException;
import com.example.securedocviewer.exception.DocumentNotFoundException;
import com.example.securedocviewer.exception.ForbiddenException;
import com.example.securedocviewer.exception.InvalidTokenException;
import com.example.securedocviewer.exception.LoginLockedException;
import com.example.securedocviewer.exception.RateLimitExceededException;
import com.example.securedocviewer.exception.ResourceNotFoundException;
import com.example.securedocviewer.exception.UsernameTakenException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.UUID;

/**
 * Every error body is JSON with an explicitly set Content-Type. Without it,
 * Spring negotiates the error body against the request's Accept header, and
 * the tile endpoint's callers may accept only images — the JSON body is then
 * "not acceptable", the handler itself fails, and a clean 401/429 turns
 * into a 500.
 *
 * <p>Messages never include stack traces, SQL, file paths or other
 * internals. Anything unexpected becomes a generic 500 carrying a short
 * reference that is also logged with the full exception, so a user's
 * report can be matched to the server log.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Keep in step with spring.servlet.multipart.max-file-size. */
    static final int MAX_UPLOAD_MB = 50;

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidToken(InvalidTokenException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }

    @ExceptionHandler({DocumentNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException e) {
        return error(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleInvalidBody(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .orElse("Invalid request.");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParameter(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request parameter.");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("Invalid request.");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableBody(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing or malformed.");
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class,
            MissingRequestHeaderException.class})
    public ResponseEntity<Map<String, String>> handleMissingInput(Exception e) {
        String name = switch (e) {
            case MissingServletRequestParameterException p -> p.getParameterName();
            case MissingServletRequestPartException p -> p.getRequestPartName();
            case MissingRequestHeaderException h -> h.getHeaderName();
            default -> "input";
        };
        return error(HttpStatus.BAD_REQUEST, "Missing required '" + name + "'.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, "Invalid value for '" + e.getName() + "'.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoRoute(NoResourceFoundException e) {
        return error(HttpStatus.NOT_FOUND, "Not found.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethod(HttpRequestMethodNotSupportedException e) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Method " + e.getMethod() + " is not supported here.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMediaType(HttpMediaTypeNotSupportedException e) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported content type.");
    }

    /** Last resort: never leak internals, but make the failure traceable in the log. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) {
        String reference = UUID.randomUUID().toString().substring(0, 8);
        log.error("Unhandled error, reference {}", reference, e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong on our side. Reference: " + reference + ".");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleUploadTooLarge(MaxUploadSizeExceededException e) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "The file is too large (limit " + MAX_UPLOAD_MB + " MB).");
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<Map<String, String>> handleUsernameTaken(UsernameTakenException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleRateLimitExceeded(RateLimitExceededException e) {
        return tooManyRequests(e.getRetryAfterSeconds(), e.getMessage());
    }

    @ExceptionHandler(LoginLockedException.class)
    public ResponseEntity<Map<String, String>> handleLoginLocked(LoginLockedException e) {
        return tooManyRequests(e.getRetryAfterSeconds(), e.getMessage());
    }

    private static ResponseEntity<Map<String, String>> tooManyRequests(long retryAfterSeconds, String message) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", message));
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", message));
    }
}
