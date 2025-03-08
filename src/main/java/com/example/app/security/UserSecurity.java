package com.example.app.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Lớp tiện ích cho bảo mật người dùng, được sử dụng trong annotations @PreAuthorize
 */
@Component("userSecurity")
public class UserSecurity {

    /**
     * Kiểm tra xem người dùng hiện tại có phải là người dùng có ID được chỉ định không
     * @param userId ID của người dùng cần kiểm tra
     * @return true nếu người dùng hiện tại có ID trùng với userId được chỉ định
     */
    public boolean isCurrentUser(Integer userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Nếu principal là một CustomUserDetails, so sánh ID
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getId().equals(userId);
        }

        // Nếu principal là String username, so sánh qua username
        return authentication.getName().equals(userId.toString());
    }
}