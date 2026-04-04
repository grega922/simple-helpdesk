package org.acme.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Login credentials")
public class LoginRequest {

    @NotBlank
    @Schema(example = "user1")
    public String username;

    @NotBlank
    @Schema(example = "password123")
    public String password;
}
