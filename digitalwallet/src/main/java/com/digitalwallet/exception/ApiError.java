package com.digitalwallet.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiError {

    private int status;
    private String error;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;

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


    public int getStatus()           { return status; }
    public String getError()         { return error; }
    public String getMessage()       { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getPath()          { return path; }
    public List<FieldError> getErrors() { return errors; }


    public record FieldError(String field, String message) {}
}
