package org.acme.helpdesk.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;

import org.acme.helpdesk.entity.User;
import org.acme.helpdesk.entity.Conversation;
import org.acme.helpdesk.entity.Message;
import org.acme.helpdesk.entity.Rooms;

import org.acme.helpdesk.dto.CreateConversationRequest;
import org.acme.helpdesk.enums.ConversationStatus;
import org.jboss.logging.Logger;

import java.util.List;
import java.time.LocalDateTime;

@ApplicationScoped
public class ConversationService {

    private static final Logger LOG = Logger.getLogger(ConversationService.class);

    //Get conversations for current user!
    public List<Conversation> getUserConversations(String username) {
        User user = User.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return Conversation.findByUser(user.id);
    }

    //Create new conversation 
    @Transactional
    public Conversation createConversation(String username, CreateConversationRequest request) {
        User user = User.findByUsername(username);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
       
        //Based on room name find room it to conversation
        Rooms room = Rooms.findByName(request.room);
        if (room == null) {
            throw new NotFoundException("Room not found");
        }

        //Create conversation and add it to postgres
        Conversation c = new Conversation();
        c.room = room;
        c.status = ConversationStatus.WAITING;
        c.user = user;
        c.createdAt = LocalDateTime.now();
        c.persist();

        //Create message and add it to postgres
        Message m = new Message();
        m.conversation = c;
        m.sender = user;
        m.senderType = user.role.name();
        m.title = request.title;
        m.content = request.message;
        m.createdAt = c.createdAt;
        m.persist();

        return c;
    }

    public Conversation getConversation(Long conversationId) {
        Conversation c = Conversation.findById(conversationId);
        if (c == null) {
            throw new NotFoundException("Conversation not found");
        }
        return c;
    }


}
