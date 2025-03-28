package com.example.app.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.app.validation.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "Product_Variant")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Variant_ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "Product_ID", nullable = false)
    @NotNull(message = "Sản phẩm không được để trống")
    private Product product;

    @Column(name = "Color", nullable = false)
    @NotBlank(message = "Màu sắc không được để trống")
    private String color;

    @Column(name = "Size", nullable = false)
    @NotBlank(message = "Kích thước không được để trống")
    private String size;

    @Column(name = "Size_Type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SizeType sizeType;

    @Column(name = "Stock_Quantity", nullable = false)
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @PositiveOrZero(message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0")
    private Integer stockQuantity = 0;

    @Column(name = "Price_Adjustment", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Điều chỉnh giá không được để trống")
    @PositiveOrZero(message = "Điều chỉnh giá phải lớn hơn hoặc bằng 0")
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @Column(name = "SKU", unique = true)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private VariantStatus status = VariantStatus.active;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> images = new HashSet<>();

    public enum VariantStatus {
        active, inactive, out_of_stock
    }
    
    public enum SizeType {
        clothing_size,
        shoe_size,
        numeric_size,
        one_size
    }
    
    @Transient
    public BigDecimal getFinalPrice() {
        if (product != null && product.getBasePrice() != null) {
            return product.getBasePrice().add(priceAdjustment);
        }
        return priceAdjustment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVariant that = (ProductVariant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}