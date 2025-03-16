package com.example.app.controller;

import com.example.app.dto.ApiResponse;
import com.example.app.dto.PasswordResetDTO;
import com.example.app.dto.PasswordResetRequestDTO;
import com.example.app.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/password")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Autowired
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * Yêu cầu đặt lại mật khẩu
     */
    @PostMapping("/reset-request")
    public ResponseEntity<ApiResponse<?>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDTO requestDTO) {

        try {
            passwordResetService.createPasswordResetTokenForUser(requestDTO.getEmail());
            return ResponseEntity.ok(ApiResponse.success(
                    "Nếu email của bạn tồn tại trong hệ thống, một email hướng dẫn đặt lại mật khẩu sẽ được gửi đến bạn"));
        } catch (Exception e) {
            // Luôn trả về thông báo thành công ngay cả khi email không tồn tại
            // Điều này giúp tránh lộ thông tin người dùng tồn tại hay không
            return ResponseEntity.ok(ApiResponse.success(
                    "Nếu email của bạn tồn tại trong hệ thống, một email hướng dẫn đặt lại mật khẩu sẽ được gửi đến bạn"));
        }
    }

    /**
     * Xác thực token đặt lại mật khẩu
     */
    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateResetToken(@RequestParam String token) {
        boolean valid = passwordResetService.validatePasswordResetToken(token);
        return ResponseEntity.ok(ApiResponse.success("Trạng thái token", valid));
    }

    /**
     * Đặt lại mật khẩu với token
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody PasswordResetDTO passwordResetDTO) {
        if (!passwordResetDTO.getNewPassword().equals(passwordResetDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Mật khẩu mới và xác nhận mật khẩu không khớp"));
        }

        boolean result = passwordResetService.resetPassword(passwordResetDTO);

        if (result) {
            return ResponseEntity.ok(ApiResponse.success("Mật khẩu đã được đặt lại thành công"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token không hợp lệ hoặc đã hết hạn"));
        }
    }
}