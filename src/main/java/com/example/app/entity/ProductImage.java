package com.example.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Product_Image")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Image_ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "Product_ID", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "Variant_ID")
    private ProductVariant variant;

    @Column(name = "Image_URL", nullable = false)
    private String imageURL;

    @Column(name = "Sort_Order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}