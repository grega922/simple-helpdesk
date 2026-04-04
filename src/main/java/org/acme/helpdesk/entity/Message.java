package org.acme.helpdesk.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class Message extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    public Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    public User sender;

    @Column(name= "sender_type" , nullable = false)
    public String senderType;

    @Column(nullable = false, length = 4000)
    public String content;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
}
