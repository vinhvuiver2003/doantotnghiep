package com.example.app.repository;


import com.example.app.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    Optional<Promotion> findByCode(String code);

    List<Promotion> findByStatus(Promotion.PromotionStatus status);

    @Query("SELECT p FROM Promotion p WHERE :now BETWEEN p.startDate AND p.endDate AND p.status = 'active'")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Promotion p WHERE :now > p.endDate AND p.status <> 'expired'")
    List<Promotion> findExpiredPromotions(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Promotion p JOIN p.categories c WHERE c.id = :categoryId AND :now BETWEEN p.startDate AND p.endDate AND p.status = 'active'")
    List<Promotion> findPromotionsByCategory(@Param("categoryId") Integer categoryId, @Param("now") LocalDateTime now);
}