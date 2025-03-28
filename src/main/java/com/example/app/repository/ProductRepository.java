package com.example.app.repository;

import com.example.app.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);

    Page<Product> findByBrandId(Integer brandId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    Page<Product> findByBasePriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    List<Product> findByStatus(Product.ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'active' ORDER BY p.createdAt DESC")
    List<Product> findNewArrivals(Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.reviews r GROUP BY p ORDER BY AVG(r.rating) DESC")
    List<Product> findTopRatedProducts(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p JOIN p.variants v WHERE v.stockQuantity < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.defaultVariant dv " +
           "LEFT JOIN FETCH dv.images dvi " +
           "LEFT JOIN FETCH p.images pi " +
           "WHERE p.id = :productId")
    Optional<Product> findByIdWithImagesAndDefaultVariant(@Param("productId") Integer productId);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.defaultVariant dv " +
           "LEFT JOIN FETCH dv.images " +
           "LEFT JOIN FETCH p.images " +
           "WHERE p.status = 'active' ORDER BY p.createdAt DESC")
    List<Product> findNewArrivalsWithImages(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.defaultVariant dv " +
           "LEFT JOIN FETCH dv.images " +
           "LEFT JOIN FETCH p.images " +
           "WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryIdWithImages(@Param("categoryId") Integer categoryId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.defaultVariant dv " +
           "LEFT JOIN FETCH dv.images " +
           "LEFT JOIN FETCH p.images " +
           "JOIN p.variants v " +
           "WHERE v.stockQuantity < :threshold")
    List<Product> findLowStockProductsWithImages(@Param("threshold") Integer threshold);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.defaultVariant dv " +
           "LEFT JOIN FETCH dv.images " +
           "LEFT JOIN FETCH p.images " +
           "LEFT JOIN FETCH p.variants v " +
           "LEFT JOIN FETCH v.images " +
           "WHERE p.id IN :productIds")
    List<Product> findByIdInWithVariantsAndImages(@Param("productIds") List<Integer> productIds);
}
