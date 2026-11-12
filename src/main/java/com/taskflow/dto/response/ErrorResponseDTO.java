
package com.taskflow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "Standard error response returned when an API request fails.")
public class ErrorResponseDTO {

    @Schema(
            description = "Date and time when the error occurred",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "2026-11-12T10:30:00"
    )
    private LocalDateTime timestamp;

    @Schema(
            description = "HTTP status code",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "400"
    )
    private int status;

    @Schema(
            description = "Short description of the error",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Bad Request"
    )
    private String error;

    @Schema(
            description = "Detailed error message. Not present in validation error responses.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Invalid request data"
    )
    private String message;

    @Schema(
            description = "Validation errors mapped by field name. Present only for validation failures.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "{\"title\":\"Title is required\"}"
    )
    private Map<String, String> fieldErrors;

    public ErrorResponseDTO() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
