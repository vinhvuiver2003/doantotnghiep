package com.example.app.repository;


import com.example.app.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}