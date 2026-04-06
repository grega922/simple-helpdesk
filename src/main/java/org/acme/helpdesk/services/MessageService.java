
package org.acme.helpdesk.service;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.acme.helpdesk.dto.CreateConversationRequest;
import org.acme.helpdesk.dto.SendMessageRequest;
import org.acme.helpdesk.entity.Conversation;
import org.acme.helpdesk.entity.Message;
import org.acme.helpdesk.entity.User;
import org.acme.helpdesk.enums.ConversationStatus;

import java.util.List;
import java.time.LocalDateTime;

@ApplicationScoped
public class MessageService {
    //List all messages in conversation (Optinally for polling get new messages)
    public List<Message> getMessages(Long conversationId, LocalDateTime since) {
        Conversation c = Conversation.findById(conversationId);
        if (c == null) {
            throw new NotFoundException("Conversation not found");
        }

        return since != null
                ? Message.find("conversation.id = ?1 and createdAt > ?2", Sort.ascending("createdAt"), conversationId, since).list()
                : Message.find("conversation.id", Sort.ascending("createdAt"), conversationId).list();
    }

    //Send message in conversation when operator made it active!
    @Transactional
    public Message sendMessage(Long conversationId, String senderUsername, SendMessageRequest request) {
        Conversation c = Conversation.findById(conversationId);
        if (c == null) {
            throw new NotFoundException("Conversation not found");
        }
        if (c.status != ConversationStatus.ACTIVE) {
            throw new BadRequestException("Conversation is not active — operator must take it first");
        }

        User sender = User.findByUsername(senderUsername);
        if (sender == null) {
            throw new NotFoundException("Sender not found");
        }

        boolean isUser = c.user.id.equals(sender.id);
        boolean isOperator = c.operator != null && c.operator.id.equals(sender.id);
        if (!isUser && !isOperator) {
            throw new ForbiddenException("You are not part of this conversation");
        }

        Message m = new Message();
        m.conversation = c;
        m.sender = sender;
        m.senderType = sender.role.name();
        m.content = request.content;
        m.createdAt = LocalDateTime.now();
        m.persist();
        return m;
    }


}
