package com.example.app.repository;

import com.example.app.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findByUserId(Integer userId, Pageable pageable);

    List<Order> findByOrderStatus(Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.orderStatus <> 'cancelled' AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalSales(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.guestEmail = :email OR (o.user.email = :email)")
    List<Order> findOrdersByEmail(@Param("emails") String email);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.orderStatus = 'delivered'")
    Long countCompletedOrdersByUser(@Param("userId") Integer userId);
}