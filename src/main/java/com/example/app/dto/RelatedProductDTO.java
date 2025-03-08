package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatedProductDTO {
    private Integer productId;
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    private Integer relatedProductId;
    private String relatedProductName;
    private String relatedProductImage;
    private BigDecimal relatedProductPrice;
    private String relationType;
}