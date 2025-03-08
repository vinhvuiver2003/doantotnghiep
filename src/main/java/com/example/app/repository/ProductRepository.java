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

    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
}
