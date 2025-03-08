package com.example.app.service;


import com.example.app.dto.PagedResponse;
import com.example.app.dto.ReviewDTO;

import java.util.List;

public interface ReviewService {
    PagedResponse<ReviewDTO> getReviewsByProduct(Integer productId, int page, int size);

    List<ReviewDTO> getReviewsByUser(Integer userId);

    ReviewDTO getReviewById(Integer id);

    ReviewDTO createReview(ReviewDTO reviewDTO);

    ReviewDTO updateReview(Integer id, ReviewDTO reviewDTO);

    void deleteReview(Integer id);

    Double calculateAverageRating(Integer productId);

    List<ReviewDTO> getRecentReviews(Integer productId, int limit);
}