package org.acme.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Login credentials to receive JWT token")
public class LoginRequest {

    @NotBlank
    @Schema(description = "Username of user/operator", example = "user1")
    public String username;

    @NotBlank
    @Schema(description = "Password for the user/operator", example = "password123")
    public String password;
}
