package org.acme.helpdesk.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import org.acme.helpdesk.enums.ConversationStatus;

@Entity
@Table(name = "conversation")
public class Conversation extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

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
