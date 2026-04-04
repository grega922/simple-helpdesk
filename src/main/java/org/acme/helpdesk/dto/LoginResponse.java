package org.acme.helpdesk.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Login response containing JWT token")
public class LoginResponse {

    @Schema(description = "JWT access token")
    public String token;

    public String username;

    public String role;

    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }
}
