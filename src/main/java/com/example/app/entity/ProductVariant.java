package com.example.app.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.app.validation.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

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

    @Column(name = "Stock_Quantity", nullable = false)
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @PositiveOrZero(message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0")
    private Integer stockQuantity = 0;

    @Column(name = "Price_Adjustment", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Điều chỉnh giá không được để trống")
    @PositiveOrZero(message = "Điều chỉnh giá phải lớn hơn hoặc bằng 0")
    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    @Column(name = "Image")
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private VariantStatus status = VariantStatus.active;

    public enum VariantStatus {
        active, inactive, out_of_stock
    }
}