package com.example.app.repository;

import com.example.app.entity.OrderItem;
import org.springframework.data.domain.Pageable;//*
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(Integer orderId);

    List<OrderItem> findByProductId(Integer productId);

    @Query("SELECT oi.product.id, SUM(oi.quantity) as totalQuantity FROM OrderItem oi GROUP BY oi.product.id ORDER BY totalQuantity DESC")
    List<Object[]> findBestSellingProducts(Pageable pageable);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.createdAt BETWEEN :startDate AND :endDate")
    List<OrderItem> findOrderItemsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}