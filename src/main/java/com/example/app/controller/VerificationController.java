package com.example.app.controller;

import com.example.app.dto.ResponseWrapper;
import com.example.app.entity.User;
import com.example.app.entity.VerificationToken;
import com.example.app.repository.UserRepository;
import com.example.app.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class VerificationController {

    private final VerificationTokenService tokenService;
    private final UserRepository userRepository;

    @Autowired
    public VerificationController(VerificationTokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @GetMapping("/verify")
    @Transactional
    public ResponseEntity<ResponseWrapper<?>> verifyEmail(@RequestParam("token") String token) {
        // Kiểm tra token có hợp lệ không
        if (!tokenService.validateToken(token)) {
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, "Token xác thực không hợp lệ hoặc đã hết hạn", null));
        }

        // Lấy token từ database
        VerificationToken verificationToken = tokenService.findByToken(token);

        // Cập nhật trạng thái người dùng (ví dụ: isEnabled = true)
        User user = verificationToken.getUser();
        // TODO: Cập nhật trạng thái người dùng tùy theo yêu cầu của ứng dụng
        // user.setEnabled(true);
        userRepository.save(user);

        // Đánh dấu token đã được sử dụng
        tokenService.markTokenAsUsed(token);

        return ResponseEntity.ok()
                .body(new ResponseWrapper<>(true, "Xác thực emails thành công", null));
    }

    @GetMapping("/resend-verification")
    public ResponseEntity<ResponseWrapper<?>> resendVerification(@RequestParam("email") String email) {
        // TODO: Triển khai logic để gửi lại emails xác thực

        return ResponseEntity.ok()
                .body(new ResponseWrapper<>(true, "Email xác thực đã được gửi lại", null));
    }
}