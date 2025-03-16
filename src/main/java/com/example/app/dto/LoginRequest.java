package com.example.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin đăng nhập người dùng")
public class LoginRequest {
    @Schema(description = "Tên đăng nhập hoặc email", example = "admin", required = true)
    @NotBlank(message = "Username or Email is required")
    private String usernameOrEmail;

    @Schema(description = "Mật khẩu", example = "admin123", required = true)
    @NotBlank(message = "Password is required")
    private String password;
}