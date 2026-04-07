package org.acme.helpdesk.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "Error type or code", example = "UNAUTHORIZED")
    public String error;
    
    @Schema(description = "Error message", example = "You are not authorized to perform this action")
    public String message;
    
    @Schema(description = "Timestamp when the error occurred", example = "2023-01-01T10:00:00")
    public LocalDateTime timestamp;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
