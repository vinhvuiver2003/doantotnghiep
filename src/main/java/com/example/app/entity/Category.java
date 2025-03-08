package com.example.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Category_ID")
    private Integer id;

    @Column(name = "Category_name", unique = true, nullable = false)
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "Image")
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Category_Parent_ID")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private Set<Category> subcategories = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private CategoryStatus status = CategoryStatus.active;

    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at")
    private LocalDateTime updatedAt;

    public enum CategoryStatus {
        active, inactive
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}