package com.example.app.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {
    private Integer id;
    private Integer productId;
    private Integer variantId;
    private String imageURL;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}