package com.example.app.controller;

import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.ReviewDTO;
import com.example.app.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Lấy danh sách đánh giá của một sản phẩm
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ResponseWrapper<PagedResponse<ReviewDTO>>> getReviewsByProduct(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<ReviewDTO> reviews = reviewService.getReviewsByProduct(productId, page, size);
        return ResponseEntity.ok(ResponseWrapper.success("Product reviews retrieved successfully", reviews));
    }

    /**
     * Lấy các đánh giá gần đây của một sản phẩm
     */
    @GetMapping("/product/{productId}/recent")
    public ResponseEntity<ResponseWrapper<List<ReviewDTO>>> getRecentReviews(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "5") int limit) {

        List<ReviewDTO> reviews = reviewService.getRecentReviews(productId, limit);
        return ResponseEntity.ok(ResponseWrapper.success("Recent reviews retrieved successfully", reviews));
    }

    /**
     * Tính điểm đánh giá trung bình của một sản phẩm
     */
    @GetMapping("/product/{productId}/average")
    public ResponseEntity<ResponseWrapper<Double>> getAverageRating(@PathVariable Integer productId) {
        Double averageRating = reviewService.calculateAverageRating(productId);
        return ResponseEntity.ok(ResponseWrapper.success("Average rating retrieved successfully", averageRating));
    }

    /**
     * Lấy danh sách đánh giá của người dùng hiện tại
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<ResponseWrapper<List<ReviewDTO>>> getMyReviews() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Lấy ID người dùng từ username (thực hiện trong ReviewService)
        List<ReviewDTO> reviews = reviewService.getReviewsByCurrentUser(username);
        return ResponseEntity.ok(ResponseWrapper.success("Your reviews retrieved successfully", reviews));
    }

    /**
     * Lấy danh sách đánh giá của một người dùng (chỉ ADMIN hoặc chính người dùng đó)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<ResponseWrapper<List<ReviewDTO>>> getReviewsByUser(@PathVariable Integer userId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(ResponseWrapper.success("User reviews retrieved successfully", reviews));
    }

    /**
     * Lấy chi tiết một đánh giá theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ReviewDTO>> getReviewById(@PathVariable Integer id) {
        ReviewDTO review = reviewService.getReviewById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Review retrieved successfully", review));
    }

    /**
     * Tạo đánh giá mới
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseWrapper<ReviewDTO>> createReview(@Valid @RequestBody ReviewDTO reviewDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Đảm bảo reviewDTO.userId trùng với người dùng hiện tại
        ReviewDTO createdReview = reviewService.createReviewByCurrentUser(username, reviewDTO);
        return new ResponseEntity<>(
                ResponseWrapper.success("Review created successfully", createdReview),
                HttpStatus.CREATED);
    }

    /**
     * Cập nhật đánh giá (chỉ tác giả của đánh giá)
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated() and @reviewSecurity.isAuthor(#id)")
    public ResponseEntity<ResponseWrapper<ReviewDTO>> updateReview(
            @PathVariable Integer id,
            @Valid @RequestBody ReviewDTO reviewDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        ReviewDTO updatedReview = reviewService.updateReviewByCurrentUser(id, username, reviewDTO);
        return ResponseEntity.ok(ResponseWrapper.success("Review updated successfully", updatedReview));
    }

    /**
     * Xóa đánh giá (ADMIN hoặc tác giả của đánh giá)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @reviewSecurity.isAuthor(#id)")
    public ResponseEntity<ResponseWrapper<?>> deleteReview(@PathVariable Integer id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ResponseWrapper.success("Review deleted successfully"));
    }
}