package com.example.app.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer categoryId;
    private String categoryName;
    private Integer brandId;
    private String brandName;
    private String productType;
    private String status;
    private Integer defaultVariantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductVariantDTO> variants;
    private List<String> images;
    private Integer totalStockQuantity;
    private Double averageRating;
    private Long reviewCount;
    private List<ReviewDTO> reviews;
}