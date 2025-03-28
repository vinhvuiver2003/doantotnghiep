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
import java.util.Optional;

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

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH i.variant v " +
           "LEFT JOIN FETCH p.defaultVariant dv " +
           "LEFT JOIN FETCH dv.images " +
           "LEFT JOIN FETCH p.images " +
           "WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Integer orderId);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH i.variant " +
           "LEFT JOIN FETCH o.delivery " +
           "LEFT JOIN FETCH o.payment " +
           "WHERE o.user.id = :userId")
    Page<Order> findByUserIdWithDetails(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH i.variant " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersByDateRangeWithItems(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items " +
           "LEFT JOIN FETCH o.delivery " +
           "LEFT JOIN FETCH o.payment " +
           "WHERE o.orderStatus = :status")
    List<Order> findByOrderStatusWithDetails(@Param("status") Order.OrderStatus status);
}