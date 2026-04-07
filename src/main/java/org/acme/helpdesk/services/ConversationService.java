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
import org.hibernate.Hibernate;

import java.util.List;
import java.time.LocalDateTime;

@ApplicationScoped
public class ConversationService {

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
        c.title = request.title;
        c.createdAt = LocalDateTime.now();
        c.persist();

        //Create message and add it to postgres
        Message m = new Message();
        m.conversation = c;
        m.sender = user;
        m.senderType = user.role.name();
        m.content = request.message;
        m.createdAt = c.createdAt;
        m.persist();

        return c;
    }

    //Get conversation by conversation ID
    public Conversation getConversation(Long conversationId) {
        Conversation c = Conversation.findById(conversationId);
        if (c == null) {
            throw new NotFoundException("Conversation not found");
        }
        return c;
    }

    //Get all conversations with waiting status
    public List<Conversation> getWaitingConversations() {
        return Conversation.findByStatus(ConversationStatus.WAITING);
    }

    //Get current operator active conversations
    public List<Conversation> getOperatorActiveConversations(String operatorName) {
        User operator = User.findByUsername(operatorName);
        if (operator == null) {
            throw new NotFoundException("Operator not found");
        }
        return Conversation.list("operator.id = ?1 and status = ?2",
                operator.id, ConversationStatus.ACTIVE);
    }

    //Operator take a user conversation to start chatting
    @Transactional
    public Conversation claimConversation(Long conversationId, String operatorUsername) {
        Conversation c = Conversation.findById(conversationId);
        if (c == null) {
            throw new NotFoundException("Conversation not found");
        }
        if (c.status != ConversationStatus.WAITING) {
            throw new BadRequestException("Conversation is not in WAITING status");
        }

        User operator = User.findByUsername(operatorUsername);
        if (operator == null) {
            throw new NotFoundException("Operator not found");
        }

        c.operator = operator;
        c.status = ConversationStatus.ACTIVE;
        c.persist();

        //This is because of Lazy loading of user and room in conversation, we need to initialize them before returning
        Hibernate.initialize(c.room);
        Hibernate.initialize(c.user);

        return c;
    }



}
