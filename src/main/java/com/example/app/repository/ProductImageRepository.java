package com.example.app.repository;

import com.example.app.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductId(Integer productId);

    List<ProductImage> findByProductIdOrderBySortOrder(Integer productId);

    List<ProductImage> findByVariantId(Integer variantId);

    @Transactional
    void deleteByProductId(Integer productId);

    @Transactional
    void deleteByVariantId(Integer variantId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM ProductImage p WHERE p.product.id = ?1 AND p.variant IS NULL")
    void deleteByProductIdAndVariantIsNull(Integer productId);
}
