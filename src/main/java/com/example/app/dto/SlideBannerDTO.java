package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlideBannerDTO {
    private Long id;
    private String title;
    private String imageUrl;
    private String linkToCategory;
    private Integer displayOrder;
    private Boolean isActive;
} 