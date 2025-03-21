package com.example.app.security;

import com.example.app.entity.Order;
import com.example.app.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Lớp tiện ích cho bảo mật đơn hàng, được sử dụng trong annotations @PreAuthorize
 */
@Component("orderSecurity")
public class OrderSecurity {

    @Autowired
    private OrderRepository orderRepository;

    public boolean isOwner(Integer orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String currentUsername = authentication.getName();

        // Lấy đơn hàng từ database
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            return false;
        }

        Order order = orderOpt.get();

        // Kiểm tra xem đơn hàng có thuộc về người dùng hiện tại không
        if (order.getUser() != null) {
            return order.getUser().getUsername().equals(currentUsername);
        }

        // Nếu là đơn hàng khách (không có user_id), kiểm tra theo emails
        return false; // Khách không đăng nhập không thể xác thực qua API
    }
}
