package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Integer id;
    private Integer cartId;
    private Integer productId;
    private String productName;
    private String productImage;
    private Integer variantId;
    private String color;
    private String size;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}