package com.example.app.repository;

import com.example.app.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductId(Integer productId);

    List<ProductImage> findByProductIdOrderBySortOrder(Integer productId);

    List<ProductImage> findByVariantId(Integer variantId);

    void deleteByProductId(Integer productId);

    void deleteByVariantId(Integer variantId);
}
