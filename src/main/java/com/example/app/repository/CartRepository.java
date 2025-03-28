package com.example.app.repository;


import com.example.app.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserId(Integer userId);

    Optional<Cart> findBySessionId(String sessionId);

    @Query("SELECT c FROM Cart c WHERE c.user IS NULL AND c.updatedAt < :expiredDate")
    List<Cart> findExpiredGuestCarts(@Param("expiredDate") LocalDateTime expiredDate);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.id = :cartId")
    Optional<Cart> findByIdWithItems(@Param("cartId") Integer cartId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.id = :cartId")
    Optional<Cart> findByIdWithItemsForUpdate(@Param("cartId") Integer cartId);
    
    // Phương thức tối ưu với fetch join đầy đủ
    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH i.variant v " +
           "LEFT JOIN FETCH p.defaultVariant dv " +
           "LEFT JOIN FETCH dv.images " +
           "LEFT JOIN FETCH p.images " +
           "WHERE c.id = :cartId")
    Optional<Cart> findByIdWithFullDetails(@Param("cartId") Integer cartId);
    
    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH i.variant v " +
           "LEFT JOIN FETCH p.defaultVariant dv " +
           "LEFT JOIN FETCH dv.images " +
           "LEFT JOIN FETCH p.images " +
           "WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithFullDetails(@Param("userId") Integer userId);
    
    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH i.variant v " +
           "LEFT JOIN FETCH p.defaultVariant dv " +
           "LEFT JOIN FETCH dv.images " +
           "LEFT JOIN FETCH p.images " +
           "WHERE c.sessionId = :sessionId")
    Optional<Cart> findBySessionIdWithFullDetails(@Param("sessionId") String sessionId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH i.variant v " +
           "WHERE c.id = :cartId")
    Optional<Cart> findByIdWithItemsAndProductsForUpdate(@Param("cartId") Integer cartId);
}