package org.acme.helpdesk.dto;

import org.acme.helpdesk.entity.Message;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Chat message details")
public class MessageResponse {

    public Long id;
    public String sender;
    public String title;
    public String content;
    public LocalDateTime createdAt;

    public MessageResponse(Long id, String sender, String title, String content, LocalDateTime createdAt) {
        this.id = id;
        this.sender = sender;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static MessageResponse from(Message m) {
        MessageResponse r = new MessageResponse(m.id, m.sender.username, m.conversation.title, m.content, m.createdAt);
        return r;
    }
}
