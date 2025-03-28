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
        // Tạo bản sao của collection để tránh ConcurrentModificationException
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
    
    /**
     * Lấy URL ảnh đại diện của sản phẩm
     * Ưu tiên theo thứ tự:
     * 1. Ảnh chính của biến thể mặc định
     * 2. Bất kỳ ảnh nào của biến thể mặc định
     * 3. Ảnh chính của sản phẩm (không thuộc về biến thể nào)
     * 4. Bất kỳ ảnh nào của sản phẩm
     * 5. URL ảnh mặc định nếu không tìm thấy ảnh nào
     * 
     * @return URL của ảnh đại diện
     */
    @Transient
    public String getMainImageUrl() {
        String imageUrl = null;
        
        // 1. Ảnh chính từ biến thể mặc định
        if (defaultVariant != null && defaultVariant.getImages() != null) {
            try {
                // Tạo bản sao để tránh ConcurrentModificationException
                Set<ProductImage> imagesCopy = new HashSet<>(defaultVariant.getImages());
                
                // Tìm ảnh đánh dấu là primary
                for (ProductImage image : imagesCopy) {
                    if (image.getIsPrimary()) {
                        return image.getImageURL();
                    }
                }
                
                // Nếu không có ảnh primary, lấy ảnh đầu tiên
                if (!imagesCopy.isEmpty()) {
                    return imagesCopy.iterator().next().getImageURL();
                }
            } catch (Exception e) {
                // Nếu có lỗi, tiếp tục với cách lấy ảnh khác
                System.err.println("Error getting default variant images: " + e.getMessage());
            }
        }
        
        // 2. Ảnh chính từ các ảnh cấp sản phẩm
        if (images != null) {
            try {
                // Tạo bản sao để tránh ConcurrentModificationException
                Set<ProductImage> imagesCopy = new HashSet<>(images);
                
                // Tìm ảnh đánh dấu là primary và không thuộc variant nào
                for (ProductImage image : imagesCopy) {
                    if (image.getVariant() == null && image.getIsPrimary()) {
                        return image.getImageURL();
                    }
                }
                
                // Nếu không có ảnh primary, lấy ảnh đầu tiên không thuộc variant nào
                for (ProductImage image : imagesCopy) {
                    if (image.getVariant() == null) {
                        return image.getImageURL();
                    }
                }
                
                // Nếu vẫn không tìm thấy, lấy bất kỳ ảnh nào
                if (!imagesCopy.isEmpty()) {
                    return imagesCopy.iterator().next().getImageURL();
                }
            } catch (Exception e) {
                // Nếu có lỗi, sử dụng ảnh mặc định
                System.err.println("Error getting product images: " + e.getMessage());
            }
        }
        
        // 3. Trả về ảnh mặc định nếu không có ảnh nào
        return "/images/default-product.jpg";
    }
}