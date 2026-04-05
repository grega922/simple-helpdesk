package org.acme.helpdesk.dto;

import org.acme.helpdesk.entity.Conversation;
import org.acme.helpdesk.enums.ConversationStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Conversation details")
public class ConversationResponse {

    public Long id;
    public String room;
    public ConversationStatus status;
    public String userName;
    public String operatorName;
    public LocalDateTime createdAt;

    public static ConversationResponse from(Conversation c) {
        ConversationResponse r = new ConversationResponse();
        r.id = c.id;
        r.room = c.room.name;
        r.userName = c.user.username;
        r.operatorName = c.operator != null ? c.operator.username : null;
        r.status = c.status;
        r.createdAt = c.createdAt;
        return r;
    }
}
