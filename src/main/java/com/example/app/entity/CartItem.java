package com.example.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Cart_Item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Cart_Item_ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "Cart_ID", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "Product_ID", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "Variant_ID", nullable = false)
    private ProductVariant variant;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "Added_at", updatable = false)
    private LocalDateTime addedAt;

    @Column(name = "Updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}