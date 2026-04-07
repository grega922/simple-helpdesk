package org.acme.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request to create a new helpdesk room")
public class CreateRoomRequest {
    
    @NotBlank
    @Schema(description = "Unique room name", example = "PODPORA")
    public String name;

    @NotBlank
    @Schema(description = "Description of the room", example = "Splošna podpora uporabnikom")
    public String description;
}
