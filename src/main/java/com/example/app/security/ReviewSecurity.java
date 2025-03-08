package com.example.app.security;
import com.example.app.entity.Review;
import com.example.app.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Lớp tiện ích cho bảo mật đánh giá, được sử dụng trong annotations @PreAuthorize
 */
@Component("reviewSecurity")
public class ReviewSecurity {

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Kiểm tra xem người dùng hiện tại có phải là tác giả của đánh giá không
     * @param reviewId ID của đánh giá cần kiểm tra
     * @return true nếu người dùng hiện tại là tác giả của đánh giá
     */
    public boolean isAuthor(Integer reviewId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();

        // Lấy đánh giá từ database
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (!reviewOpt.isPresent()) {
            return false;
        }

        Review review = reviewOpt.get();

        // Kiểm tra xem đánh giá có thuộc về người dùng hiện tại không
        return review.getUser().getUsername().equals(currentUsername);
    }
}