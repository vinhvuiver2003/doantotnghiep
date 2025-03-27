package com.example.app.security;

import com.example.app.entity.Cart;
import com.example.app.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Lớp tiện ích cho bảo mật giỏ hàng, được sử dụng trong annotations @PreAuthorize
 */
@Component("cartSecurity")
public class CartSecurity {

    @Autowired
    private CartRepository cartRepository;

    /**
     * Kiểm tra xem người dùng hiện tại có phải là chủ sở hữu của giỏ hàng không
     * @param cartId ID của giỏ hàng cần kiểm tra
     * @return true nếu người dùng hiện tại là chủ sở hữu hoặc giỏ hàng thuộc cùng sessionId
     */
    public boolean isOwner(Integer cartId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Lấy giỏ hàng từ database
        Optional<Cart> cartOpt = cartRepository.findById(cartId);
        if (!cartOpt.isPresent()) {
            return false;
        }

        Cart cart = cartOpt.get();

        // Nếu giỏ hàng có user
        if (cart.getUser() != null && authentication != null && authentication.isAuthenticated() 
                && !authentication.getPrincipal().equals("anonymousUser")) {
            // Lấy tên người dùng hiện tại
            String currentUsername = authentication.getName();
            
            // Kiểm tra xem giỏ hàng có thuộc về người dùng hiện tại không
            return cart.getUser().getUsername().equals(currentUsername);
        }

        // Nếu là giỏ hàng khách hàng (không có user_id), API sẽ cần sessionId để xác thực
        // SessionId nên được truyền như một parameter trong request
        // Việc kiểm tra sessionId được thực hiện trong controller
        return true; // Cho phép truy cập công khai, kiểm tra sẽ được thực hiện trong controller
    }
} 