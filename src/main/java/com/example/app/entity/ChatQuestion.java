package com.example.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_questions")
@Data
public class ChatQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private Integer frequency = 1;

    @Column(name = "last_asked_at")
    private LocalDateTime lastAskedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastAskedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastAskedAt = LocalDateTime.now();
    }
} 