package com.example.app.repository;

import com.example.app.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    List<ProductVariant> findByProductId(Integer productId);

    Optional<ProductVariant> findByProductIdAndColorAndSize(Integer productId, String color, String size);

    List<ProductVariant> findByStatus(ProductVariant.VariantStatus status);

    @Query("SELECT v FROM ProductVariant v WHERE v.stockQuantity < :threshold")
    List<ProductVariant> findLowStockVariants(@Param("threshold") Integer threshold);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.id = :productId AND v.stockQuantity > 0")
    List<ProductVariant> findAvailableVariants(@Param("productId") Integer productId);
    
    // Các phương thức tối ưu sử dụng fetch join
    @Query("SELECT DISTINCT v FROM ProductVariant v " +
           "LEFT JOIN FETCH v.images " +
           "WHERE v.id = :variantId")
    Optional<ProductVariant> findByIdWithImages(@Param("variantId") Integer variantId);
    
    @Query("SELECT DISTINCT v FROM ProductVariant v " +
           "LEFT JOIN FETCH v.images " +
           "WHERE v.product.id = :productId")
    List<ProductVariant> findByProductIdWithImages(@Param("productId") Integer productId);
    
    @Query("SELECT DISTINCT v FROM ProductVariant v " +
           "LEFT JOIN FETCH v.images " +
           "WHERE v.product.id = :productId AND v.stockQuantity > 0")
    List<ProductVariant> findAvailableVariantsWithImages(@Param("productId") Integer productId);
    
    @Query("SELECT DISTINCT v FROM ProductVariant v " +
           "LEFT JOIN FETCH v.images " +
           "WHERE v.stockQuantity < :threshold")
    List<ProductVariant> findLowStockVariantsWithImages(@Param("threshold") Integer threshold);
    
    @Query("SELECT DISTINCT v FROM ProductVariant v " +
           "LEFT JOIN FETCH v.images " +
           "WHERE v.product.id = :productId AND v.color = :color AND v.size = :size")
    Optional<ProductVariant> findByProductIdAndColorAndSizeWithImages(
            @Param("productId") Integer productId, 
            @Param("color") String color, 
            @Param("size") String size);
}
