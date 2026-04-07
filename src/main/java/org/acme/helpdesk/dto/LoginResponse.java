package org.acme.helpdesk.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Login response containing JWT token")
public class LoginResponse {

    @Schema(description = "JWT access token", example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJza...")
    public String token;

    @Schema(description = "Username of the logged-in user/operator", example = "user1")
    public String username;

    @Schema(description = "Role of the logged-in user/operator", example = "USER")
    public String role;

    @Schema(description = "Token validity in seconds", example = "86000")
    public Long expire;

    public LoginResponse(String token, String username, String role, Long expire) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.expire = expire;
    }
}
