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
public class PromotionDTO {
    private Integer id;
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private String code;
    private BigDecimal minimumOrder;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usageCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CategoryDTO> categories;
}
