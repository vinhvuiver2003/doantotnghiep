package com.example.app.service.impl;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.ReviewDTO;
import com.example.app.entity.Product;
import com.example.app.entity.Review;
import com.example.app.entity.User;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.OrderItemRepository;
import com.example.app.repository.OrderRepository;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.ReviewRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public ReviewServiceImpl(
            ReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public PagedResponse<ReviewDTO> getReviewsByProduct(Integer productId, int page, int size) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);

        List<ReviewDTO> content = reviews.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                reviews.getNumber(),
                reviews.getSize(),
                reviews.getTotalElements(),
                reviews.getTotalPages(),
                reviews.isLast()
        );
    }

    @Override
    public List<ReviewDTO> getReviewsByUser(Integer userId) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Review> reviews = reviewRepository.findByUserId(userId);

        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDTO getReviewById(Integer id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        return convertToDTO(review);
    }

    @Override
    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + reviewDTO.getProductId()));

        User user = userRepository.findById(reviewDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + reviewDTO.getUserId()));

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(reviewDTO.getRating());
        
        review.setTitle(reviewDTO.getTitle());
        review.setContent(reviewDTO.getContent());
        
        review.setComment(reviewDTO.getComment());

        Review savedReview = reviewRepository.save(review);

        updateProductRating(product.getId());

        return convertToDTO(savedReview);
    }

    @Override
    @Transactional
    public ReviewDTO updateReview(Integer id, ReviewDTO reviewDTO) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        if (!review.getUser().getId().equals(reviewDTO.getUserId())) {
            throw new IllegalArgumentException("User does not own this review");
        }

        if (reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());

        Review updatedReview = reviewRepository.save(review);

        return convertToDTO(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Integer id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review not found with id: " + id);
        }

        reviewRepository.deleteById(id);
    }

    @Override
    public Double calculateAverageRating(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Double avgRating = reviewRepository.calculateAverageRating(productId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Override
    public List<ReviewDTO> getRecentReviews(Integer productId, int limit) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Pageable pageable = PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findRecentReviews(productId, pageable);

        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        dto.setUserId(review.getUser().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setContent(review.getContent());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        return dto;
    }
    @Override
    public List<ReviewDTO> getReviewsByCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return getReviewsByUser(user.getId());
    }

    @Override
    @Transactional
    public ReviewDTO createReviewByCurrentUser(String username, ReviewDTO reviewDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        reviewDTO.setUserId(user.getId());

        List<Review> existingReviews = reviewRepository.findByProductId(reviewDTO.getProductId(), PageRequest.of(0, 1000)).getContent();
        boolean hasReviewed = existingReviews.stream()
                .anyMatch(review -> review.getUser().getId().equals(user.getId()));

        if (hasReviewed) {
            throw new IllegalArgumentException("You have already reviewed this product");
        }

        return createReview(reviewDTO);
    }

    @Override
    @Transactional
    public ReviewDTO updateReviewByCurrentUser(Integer id, String username, ReviewDTO reviewDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You are not authorized to update this review");
        }

        reviewDTO.setProductId(review.getProduct().getId());
        reviewDTO.setUserId(user.getId());

        return updateReview(id, reviewDTO);
    }


    private void updateProductRating(Integer productId) {
        Double avgRating = calculateAverageRating(productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        

    }
}