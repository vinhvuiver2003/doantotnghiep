package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Integer id;
    private String name;
    private String description;
    private String image;
    private Integer parentId;
    private String parentName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CategoryDTO> subcategories;
}