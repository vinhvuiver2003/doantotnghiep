package com.example.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "Order_Item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Order_Item_ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "Order_ID", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "Product_ID", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "Variant_ID", nullable = false)
    private ProductVariant variant;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "Unit_Price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "Discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "Total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
}