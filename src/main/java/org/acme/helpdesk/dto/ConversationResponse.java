package org.acme.helpdesk.dto;

import org.acme.helpdesk.entity.Conversation;
import org.acme.helpdesk.enums.ConversationStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Conversation details")
public class ConversationResponse {

    @Schema(description = "Unique identifier of the conversation", example = "1")
    public Long id;
    
    
    @Schema(description = "Room in which conversation was created", example = "TEHNIKA")
    public String room;

    @Schema(description = "Status of the conversation", example = "WAITING")
    public ConversationStatus status;
    
    @Schema(description = "Username of the user who created the conversation", example = "user1")
    public String userName;
    
    @Schema(description = "Title of the conversation", example = "Naslov sporočila")
    public String title;
    
    @Schema(description = "Username of the operator handling the conversation", example = "operator1")
    public String operatorName;
    
    @Schema(description = "Timestamp when the conversation was created", example = "2023-01-01T10:00:00")
    public LocalDateTime createdAt;

    public static ConversationResponse from(Conversation c) {
        ConversationResponse r = new ConversationResponse();
        r.id = c.id;
        r.room = c.room.name;
        r.userName = c.user.username;
        r.title = c.title;
        r.operatorName = c.operator != null ? c.operator.username : null;
        r.status = c.status;
        r.createdAt = c.createdAt;
        return r;
    }
}
