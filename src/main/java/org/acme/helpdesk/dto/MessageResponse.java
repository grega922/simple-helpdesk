package org.acme.helpdesk.dto;

import org.acme.helpdesk.entity.Message;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Chat message details")
public class MessageResponse {

    @Schema(description = "Unique identifier of the message", example = "1")
    public Long id;

    @Schema(description = "ID of the conversation to which the message belongs", example = "2")
    public Long conversationId;
    
    @Schema(description = "Username of the sender/user", example = "user1")
    public String sender;
    
    @Schema(description = "Role of the sender (e.g. USER, OPERATOR)", example = "USER")
    public String senderRole;

    @Schema(description = "Title of the message", example = "Naslov sporočila")
    public String title;

    @Schema(description = "Content of the message", example = "Imam težavo z nastavitvami računa.")
    public String content;
    
    @Schema(description = "Timestamp when the message was created", example = "2023-01-01T10:00:00")
    public LocalDateTime createdAt;

    public MessageResponse(Long id, Long conversationId, String sender, String senderRole, String title, String content, LocalDateTime createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.sender = sender;
        this.senderRole = senderRole;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static MessageResponse from(Message m) {
        MessageResponse r = new MessageResponse(m.id, m.conversation.id, m.sender.username, m.sender.role.name(), m.conversation.title, m.content, m.createdAt);
        return r;
    }
}
