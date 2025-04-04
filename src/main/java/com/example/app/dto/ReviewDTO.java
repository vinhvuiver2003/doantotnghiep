package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Integer id;
    private Integer productId;
    private String productName;
    private Integer userId;
    private String username;
    private Integer rating;
    private String title;
    private String content;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}