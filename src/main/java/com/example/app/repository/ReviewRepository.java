package com.example.app.repository;
import com.example.app.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Page<Review> findByProductId(Integer productId, Pageable pageable);

    List<Review> findByUserId(Integer userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double calculateAverageRating(@Param("productId") Integer productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.rating >= :minRating")
    Long countPositiveReviews(@Param("productId") Integer productId, @Param("minRating") Integer minRating);

    @Query("SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.createdAt DESC")
    List<Review> findRecentReviews(@Param("productId") Integer productId, Pageable pageable);
}