package org.acme.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request to send a message in a conversation")
public class SendMessageRequest {

    @NotBlank
    @Schema(example = "Hvala za odgovor, bom preveril.")
    public String content;
}
