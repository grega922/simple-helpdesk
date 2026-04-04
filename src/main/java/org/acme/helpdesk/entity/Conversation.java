package org.acme.helpdesk.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

//Defining user roles (Can be part )
public enum ConversationStatus {
    WAITING,
    ACTIVE,
    CLOSED
}

@Entity
@Table(name = "conversation")
public class Conversation extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    public Rooms room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    public User operator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ConversationStatus status;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
    
}
