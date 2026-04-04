package org.acme.helpdesk.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Standard error response")
public class ErrorResponse {

    public String error;
    public String message;
    public int status;
    public LocalDateTime timestamp;

    public ErrorResponse(String error, String message, int status) {
        this.error = error;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
