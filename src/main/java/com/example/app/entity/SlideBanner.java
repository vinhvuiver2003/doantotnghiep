package com.example.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "slide_banners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlideBanner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String linkToCategory;

    @Column(nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 