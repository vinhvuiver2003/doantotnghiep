package com.example.app.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Integer id;
    private Integer orderId;
    private Integer productId;
    private String productName;
    private String productImage;
    private Integer variantId;
    private String color;
    private String size;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal total;
}