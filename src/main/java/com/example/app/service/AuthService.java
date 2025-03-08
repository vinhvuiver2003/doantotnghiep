package com.example.app.service;

// AuthService Interface


import com.example.app.dto.LoginRequest;
import com.example.app.dto.LoginResponse;
import com.example.app.dto.UserCreateDTO;
import com.example.app.dto.UserDTO;

public interface AuthService {
    /**
     * Xử lý đăng nhập người dùng
     * @param loginRequest thông tin đăng nhập
     * @return JWT token và thông tin người dùng
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * Đăng ký tài khoản mới
     * @param registerRequest thông tin người dùng mới
     * @return thông tin người dùng đã đăng ký
     */
    UserDTO register(UserCreateDTO registerRequest);

    /**
     * Lấy thông tin người dùng hiện tại từ Authentication
     * @return thông tin người dùng
     */
    UserDTO getCurrentUser();

    /**
     * Xác thực JWT token
     * @param token JWT token
     * @return true nếu token hợp lệ, false nếu không
     */
    boolean validateToken(String token);

    /**
     * Lấy thông tin người dùng từ JWT token
     * @param token JWT token
     * @return thông tin người dùng hoặc null nếu token không hợp lệ
     */
    UserDTO getUserByToken(String token);

    /**
     * Cập nhật thời gian đăng nhập cuối
     * @param userId ID của người dùng
     */
    void updateLastLogin(Integer userId);
}