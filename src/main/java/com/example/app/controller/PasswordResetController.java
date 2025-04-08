package com.example.app.controller;

import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.PasswordResetDTO;
import com.example.app.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Autowired
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseWrapper<?>> forgotPassword(@RequestParam("email") String email) {
        try {
            passwordResetService.createPasswordResetTokenForUser(email);
            return ResponseEntity.ok(ResponseWrapper.success("Nếu emails này đã được đăng ký, chúng tôi đã gửi cho bạn hướng dẫn đặt lại mật khẩu"));
        } catch (Exception e) {
            // Trả về thông báo thành công dù có lỗi, để tránh rò rỉ thông tin tài khoản
            return ResponseEntity.ok(ResponseWrapper.success("Nếu emails này đã được đăng ký, chúng tôi đã gửi cho bạn hướng dẫn đặt lại mật khẩu"));
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<ResponseWrapper<Boolean>> validateResetToken(@RequestParam("token") String token) {
        boolean isValid = passwordResetService.validatePasswordResetToken(token);
        return ResponseEntity.ok(ResponseWrapper.success("Kết quả xác thực token", isValid));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseWrapper<?>> resetPassword(@RequestBody PasswordResetDTO passwordResetDTO) {
        if (!passwordResetService.validatePasswordResetToken(passwordResetDTO.getToken())) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Token không hợp lệ hoặc đã hết hạn"));
        }

        if (!passwordResetDTO.getNewPassword().equals(passwordResetDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Mật khẩu mới và xác nhận mật khẩu không khớp"));
        }

        boolean result = passwordResetService.resetPassword(passwordResetDTO);

        if (result) {
            return ResponseEntity.ok(ResponseWrapper.success("Mật khẩu đã được đặt lại thành công"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ResponseWrapper.error("Không thể đặt lại mật khẩu"));
        }
    }
}