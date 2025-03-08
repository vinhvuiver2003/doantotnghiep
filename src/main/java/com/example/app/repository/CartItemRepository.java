package com.example.app.repository;


import com.example.app.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCartId(Integer cartId);

    Optional<CartItem> findByCartIdAndProductIdAndVariantId(Integer cartId, Integer productId, Integer variantId);

    void deleteByCartId(Integer cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.product.id = :productId")
    List<CartItem> findByProductId(@Param("productId") Integer productId);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Long countItemsInCart(@Param("cartId") Integer cartId);

    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer getTotalQuantityInCart(@Param("cartId") Integer cartId);
}