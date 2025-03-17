package com.example.app.security;

import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Lớp tiện ích cho bảo mật người dùng, được sử dụng trong annotations @PreAuthorize
 */
@Component("userSecurity")
public class UserSecurity {

    @Autowired
    private UserRepository userRepository;

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

        // Nếu principal là String username, tìm user từ database và so sánh ID
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }
}