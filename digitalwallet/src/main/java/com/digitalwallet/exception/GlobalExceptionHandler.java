package com.digitalwallet.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Central place for all exception-to-HTTP-response mappings.
 *
 * The goal is that nothing outside this class ever calls ResponseEntity.badRequest()
 * or sets an error status directly. Controllers just throw, this handles it.
 *
 * Ordering matters — Spring picks the most specific handler available,
 * so subclasses should come before their parents in the method list.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -----------------------------------------------------------------------
    // Domain exceptions
    // -----------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found: {}", ex.getMessage());
        ApiError error = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiError> handleInsufficientFunds(
            InsufficientFundsException ex, HttpServletRequest request) {

        log.warn("Insufficient funds: {}", ex.getMessage());
        ApiError error = new ApiError(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Insufficient Funds",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(WalletOperationException.class)
    public ResponseEntity<ApiError> handleWalletOperation(
            WalletOperationException ex, HttpServletRequest request) {

        log.warn("Wallet operation blocked: {}", ex.getMessage());
        ApiError error = new ApiError(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Wallet Operation Failed",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {

        log.warn("Duplicate resource: {}", ex.getMessage());
        ApiError error = new ApiError(
            HttpStatus.CONFLICT.value(),
            "Conflict",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // -----------------------------------------------------------------------
    // Validation exceptions
    // -----------------------------------------------------------------------

    /**
     * Handles @Valid failures on @RequestBody DTOs.
     * Collects all field errors so the client gets everything at once
     * instead of fixing one thing and hitting a new error.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "One or more fields failed validation",
            request.getRequestURI()
        );

        BindingResult binding = ex.getBindingResult();
        binding.getFieldErrors().forEach(fe ->
            error.addFieldError(fe.getField(), fe.getDefaultMessage())
        );
        // Also capture class-level constraints if any
        binding.getGlobalErrors().forEach(ge ->
            error.addFieldError(ge.getObjectName(), ge.getDefaultMessage())
        );

        log.warn("Validation failed for request to {}: {} error(s)",
            request.getRequestURI(), binding.getErrorCount());

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles constraint violations on @RequestParam / @PathVariable (when
     * @Validated is on the controller class).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Constraint violation",
            request.getRequestURI()
        );

        ex.getConstraintViolations().forEach(cv -> {
            String field = cv.getPropertyPath().toString();
            // Strip method name prefix from the path (e.g. "methodName.fieldName" → "fieldName")
            int dot = field.lastIndexOf('.');
            error.addFieldError(dot >= 0 ? field.substring(dot + 1) : field, cv.getMessage());
        });

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String msg = String.format("Parameter '%s' must be of type %s",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            msg,
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    // -----------------------------------------------------------------------
    // Security exceptions
    // -----------------------------------------------------------------------

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiError> handleSecurityException(
            SecurityException ex, HttpServletRequest request) {

        ApiError error = new ApiError(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {

        // Intentionally vague — we don't want to leak whether the email or password was wrong
        ApiError error = new ApiError(
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            "Invalid email or password",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        ApiError error = new ApiError(
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            "You don't have permission to access this resource",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // -----------------------------------------------------------------------
    // Database exceptions
    // -----------------------------------------------------------------------

    /**
     * Catches raw DB constraint violations that slip past service-layer checks.
     * We log the real error but show a safe message to the client.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.error("Data integrity violation at {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError error = new ApiError(
            HttpStatus.CONFLICT.value(),
            "Data Conflict",
            "A record with the provided data already exists",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // -----------------------------------------------------------------------
    // Catch-all
    // -----------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex, HttpServletRequest request) {

        // Log the full stack trace here — this is unexpected and we want to know about it
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);

        ApiError error = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "Something went wrong. Please try again later.",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
