package com.example.app.controller;

import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.CartDTO;
import com.example.app.dto.CartItemDTO;
import com.example.app.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    // Lấy giỏ hàng của nguời dùng, nếu chưa có thì tạo mói
    @GetMapping("/my-cart")
    public ResponseEntity<ResponseWrapper<CartDTO>> getMyCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String username = authentication.getName();
            CartDTO cart = cartService.getCartByCurrentUser(username);
            return ResponseEntity.ok(ResponseWrapper.success("Cart retrieved successfully", cart));
        } else {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((ResponseWrapper<CartDTO>) ResponseWrapper.error("SessionId is required for guest cart"));
        }
    }


    @GetMapping("/guest-cart")
    public ResponseEntity<ResponseWrapper<CartDTO>> getGuestCart(@RequestParam String sessionId) {
        CartDTO cart = cartService.getCartBySessionId(sessionId);
        return ResponseEntity.ok(ResponseWrapper.success("Guest cart retrieved successfully", cart));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<CartDTO>> getCartById(@PathVariable Integer id) {
        CartDTO cart = cartService.getCartById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Cart retrieved successfully", cart));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<ResponseWrapper<CartDTO>> addItemToCart(
            @PathVariable Integer cartId,
            @Valid @RequestBody CartItemDTO cartItemDTO) {

        if (cartItemDTO.getQuantity() != null && cartItemDTO.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body(ResponseWrapper.error("Số lượng sản phẩm phải lớn hơn 0", CartDTO.class));
        }

        CartDTO updatedCart = cartService.addItemToCart(cartId, cartItemDTO);
        return ResponseEntity.ok(ResponseWrapper.success("Item added to cart successfully", updatedCart));
    }


    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<ResponseWrapper<CartDTO>> updateCartItem(
            @PathVariable Integer cartId,
            @PathVariable Integer itemId,
            @RequestParam Integer quantity) {

        if (quantity < 0) {
            return ResponseEntity.badRequest().body(ResponseWrapper.error("Số lượng sản phẩm không được âm", CartDTO.class));
        }

        CartDTO updatedCart = cartService.updateCartItem(cartId, itemId, quantity);
        return ResponseEntity.ok(ResponseWrapper.success("Cart item updated successfully", updatedCart));
    }


    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<ResponseWrapper<CartDTO>> removeItemFromCart(
            @PathVariable Integer cartId,
            @PathVariable Integer itemId) {

        CartDTO updatedCart = cartService.removeItemFromCart(cartId, itemId);
        return ResponseEntity.ok(ResponseWrapper.success("Item removed from cart successfully", updatedCart));
    }


    @DeleteMapping("/{cartId}/clear")
    public ResponseEntity<ResponseWrapper<?>> clearCart(@PathVariable Integer cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok(ResponseWrapper.success("Cart cleared successfully"));
    }


    @PostMapping("/merge")
    public ResponseEntity<ResponseWrapper<CartDTO>> mergeGuestCart(
            @RequestParam String sessionId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // Sử dụng cast để khớp kiểu dữ liệu
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body((ResponseWrapper<CartDTO>) ResponseWrapper.error("User must be logged in to merge carts"));
        }

        String username = authentication.getName();
        CartDTO mergedCart = cartService.mergeGuestCartWithUserCart(sessionId, username);

        return ResponseEntity.ok(ResponseWrapper.success("Carts merged successfully", mergedCart));
    }
}