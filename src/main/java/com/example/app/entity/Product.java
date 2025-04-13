package com.example.app.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.app.validation.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Product_ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "Category_ID", nullable = false)
    @NotNull(message = "Danh mục không được để trống")
    private Category category;

    @Column(name = "Name", nullable = false)
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "Base_Price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Giá không được để trống")
    @PositiveOrZero(message = "Giá phải lớn hơn hoặc bằng 0")
    private BigDecimal basePrice;

    @ManyToOne
    @JoinColumn(name = "Brand_ID", nullable = false)
    @NotNull(message = "Thương hiệu không được để trống")
    private Brand brand;

    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at")
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "Default_Variant_ID")
    private ProductVariant defaultVariant;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private ProductStatus status = ProductStatus.active;

    @Enumerated(EnumType.STRING)
    @Column(name = "Product_Type", nullable = false)
    private ProductType productType;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductVariant> variants = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> images = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews = new HashSet<>();

    public enum ProductStatus {
        active, inactive, discontinued
    }

    public enum ProductType {
        clothing, footwear, accessory
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
    
    @Transient
    public Integer getTotalStockQuantity() {

        Set<ProductVariant> variantsCopy = new HashSet<>(variants);
        return variantsCopy.stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    

    @Transient
    public String getMainImageUrl() {
        String imageUrl = null;
        
        // 1. Ảnh chính từ biến thể mặc định
        if (defaultVariant != null && defaultVariant.getImages() != null) {
            try {

                Set<ProductImage> imagesCopy = new HashSet<>(defaultVariant.getImages());

                for (ProductImage image : imagesCopy) {
                    if (image.getIsPrimary()) {
                        return image.getImageURL();
                    }
                }

                if (!imagesCopy.isEmpty()) {
                    return imagesCopy.iterator().next().getImageURL();
                }
            } catch (Exception e) {

                System.err.println("Error getting default variant images: " + e.getMessage());
            }
        }
        if (images != null) {
            try {
                Set<ProductImage> imagesCopy = new HashSet<>(images);
                

                for (ProductImage image : imagesCopy) {
                    if (image.getVariant() == null && image.getIsPrimary()) {
                        return image.getImageURL();
                    }
                }
                
                for (ProductImage image : imagesCopy) {
                    if (image.getVariant() == null) {
                        return image.getImageURL();
                    }
                }
                
                if (!imagesCopy.isEmpty()) {
                    return imagesCopy.iterator().next().getImageURL();
                }
            } catch (Exception e) {
                System.err.println("Error getting product images: " + e.getMessage());
            }
        }
        
        return "/images/default-product.jpg";
    }
}