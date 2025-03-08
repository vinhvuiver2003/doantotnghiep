package com.example.app.service;

import com.example.app.dto.CartDTO;
import com.example.app.dto.CartItemDTO;

import java.util.List;

public interface CartService {
    CartDTO getCartById(Integer id);

    CartDTO getCartByUserId(Integer userId);

    CartDTO getCartBySessionId(String sessionId);

    CartDTO createCart(Integer userId, String sessionId);

    CartDTO addItemToCart(Integer cartId, CartItemDTO cartItemDTO);

    CartDTO updateCartItem(Integer cartId, Integer cartItemId, Integer quantity);

    CartDTO removeItemFromCart(Integer cartId, Integer cartItemId);

    void clearCart(Integer cartId);

    void deleteExpiredCarts(int expirationDays);

    void mergeGuestCartWithUserCart(String sessionId, Integer userId);

    CartDTO getCartByCurrentUser(String username);
    CartDTO mergeGuestCartWithUserCart(String sessionId, String username);
}

