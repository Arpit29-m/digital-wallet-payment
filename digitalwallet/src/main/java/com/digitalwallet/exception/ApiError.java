package com.digitalwallet.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard error envelope returned by every error response in the API.
 *
 * Having a single error shape means frontend developers know exactly
 * what to expect — no guessing whether it's a string, object, or array.
 *
 * The 'errors' list is only included when there are field-level validation
 * errors (so it won't clutter simple 404 responses).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiError {

    private int status;
    private String error;       // e.g. "Not Found", "Bad Request"
    private String message;     // human-readable explanation

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;        // request URI, helpful for debugging

    // Field-level validation errors (null / empty = omitted from JSON)
    private List<FieldError> errors;

    public ApiError(int status, String error, String message, String path) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.path      = path;
        this.timestamp = LocalDateTime.now();
    }

    public void addFieldError(String field, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new FieldError(field, message));
    }

    // --- getters (no setters exposed beyond what Jackson needs) ---

    public int getStatus()           { return status; }
    public String getError()         { return error; }
    public String getMessage()       { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPath()          { return path; }
    public List<FieldError> getErrors() { return errors; }

    // Inner class for individual field errors
    public record FieldError(String field, String message) {}
}
