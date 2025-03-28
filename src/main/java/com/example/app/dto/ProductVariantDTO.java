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
    private String sizeType; // clothing_size, shoe_size, numeric_size, one_size
    private Integer stockQuantity;
    private BigDecimal priceAdjustment;
    private BigDecimal finalPrice; // basePrice + priceAdjustment
    private String sku; // Thêm: Stock Keeping Unit
    private String status;
    private List<ProductImageDTO> images; // Thay đổi: danh sách đầy đủ các ảnh, không chỉ URLs
    private Boolean isPrimary; // Thêm: biến thể này có phải là mặc định không
}
