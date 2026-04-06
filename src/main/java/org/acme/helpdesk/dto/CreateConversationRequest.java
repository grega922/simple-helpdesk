package org.acme.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request to create a new helpdesk conversation")
public class CreateConversationRequest {

    @NotNull
    @Schema(example = "TEHNIKA")
    public String room;

    @NotBlank
    @Schema(example = "Naslov sporočila")
    public String title;

    @NotBlank
    @Schema(example = "Imam težavo z nastavitvami računa.")
    public String message;
}
