package com.example.app.entity;
import jakarta.persistence.*;
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
    private Product product;

    @Column(name = "Color", nullable = false)
    private String color;

    @Column(name = "Size", nullable = false)
    private String size;

    @Column(name = "Stock_Quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "Price_Adjustment", nullable = false, precision = 10, scale = 2)
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