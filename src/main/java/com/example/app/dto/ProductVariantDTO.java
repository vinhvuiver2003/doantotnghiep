package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {
    private Integer id;
    private Integer productId;
    private String color;
    private String size;
    private Integer stockQuantity;
    private BigDecimal priceAdjustment;
    private BigDecimal finalPrice; // basePrice + priceAdjustment
    private String image;
    private String status;
    private List<String> images;
}
