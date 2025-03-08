package com.example.app.controller;

import com.example.app.dto.ApiResponse;
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

    /**
     * Lấy giỏ hàng của người dùng hiện tại hoặc tạo mới nếu chưa có
     */
    @GetMapping("/my-cart")
    public ResponseEntity<ApiResponse<CartDTO>> getMyCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            // Người dùng đã đăng nhập
            String username = authentication.getName();
            CartDTO cart = cartService.getCartByCurrentUser(username);
            return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
        } else {
            // Khách chưa đăng nhập - cần session ID
            // Sử dụng cast để khớp kiểu dữ liệu
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((ApiResponse<CartDTO>) ApiResponse.error("SessionId is required for guest cart"));
        }
    }

    /**
     * Lấy giỏ hàng của khách (chưa đăng nhập) theo sessionId
     */
    @GetMapping("/guest-cart")
    public ResponseEntity<ApiResponse<CartDTO>> getGuestCart(@RequestParam String sessionId) {
        CartDTO cart = cartService.getCartBySessionId(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Guest cart retrieved successfully", cart));
    }

    /**
     * Lấy giỏ hàng theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CartDTO>> getCartById(@PathVariable Integer id) {
        CartDTO cart = cartService.getCartById(id);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @PostMapping("/{cartId}/items")
    public ResponseEntity<ApiResponse<CartDTO>> addItemToCart(
            @PathVariable Integer cartId,
            @Valid @RequestBody CartItemDTO cartItemDTO) {

        CartDTO updatedCart = cartService.addItemToCart(cartId, cartItemDTO);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", updatedCart));
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateCartItem(
            @PathVariable Integer cartId,
            @PathVariable Integer itemId,
            @RequestParam Integer quantity) {

        CartDTO updatedCart = cartService.updateCartItem(cartId, itemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", updatedCart));
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDTO>> removeItemFromCart(
            @PathVariable Integer cartId,
            @PathVariable Integer itemId) {

        CartDTO updatedCart = cartService.removeItemFromCart(cartId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", updatedCart));
    }

    /**
     * Xóa tất cả sản phẩm trong giỏ hàng
     */
    @DeleteMapping("/{cartId}/clear")
    public ResponseEntity<ApiResponse<?>> clearCart(@PathVariable Integer cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }

    /**
     * Chuyển đổi giỏ hàng khách thành giỏ hàng của người dùng sau khi đăng nhập
     */
    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<CartDTO>> mergeGuestCart(
            @RequestParam String sessionId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // Sử dụng cast để khớp kiểu dữ liệu
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body((ApiResponse<CartDTO>) ApiResponse.error("User must be logged in to merge carts"));
        }

        String username = authentication.getName();
        CartDTO mergedCart = cartService.mergeGuestCartWithUserCart(sessionId, username);

        return ResponseEntity.ok(ApiResponse.success("Carts merged successfully", mergedCart));
    }
}