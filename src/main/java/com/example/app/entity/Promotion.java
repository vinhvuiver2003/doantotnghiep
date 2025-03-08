package com.example.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Promotion_ID")
    private Integer id;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "Discount_Type", nullable = false)
    private DiscountType discountType;

    @Column(name = "Discount_Value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "Code", unique = true)
    private String code;

    @Column(name = "Minimum_Order", precision = 10, scale = 2)
    private BigDecimal minimumOrder = BigDecimal.ZERO;

    @Column(name = "Start_Date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "End_Date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "Usage_Limit")
    private Integer usageLimit;

    @Column(name = "Usage_Count", nullable = false)
    private Integer usageCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private PromotionStatus status = PromotionStatus.active;

    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "Promotion_Category",
            joinColumns = @JoinColumn(name = "Promotion_ID"),
            inverseJoinColumns = @JoinColumn(name = "Category_ID")
    )
    private Set<Category> categories = new HashSet<>();

    public enum DiscountType {
        percentage, fixed_amount
    }

    public enum PromotionStatus {
        active, inactive, expired
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