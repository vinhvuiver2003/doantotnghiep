package com.example.app.service.impl;

import com.example.app.dto.PasswordResetDTO;
import com.example.app.entity.PasswordResetToken;
import com.example.app.entity.User;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.PasswordResetTokenRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.EmailService;
import com.example.app.service.PasswordResetService;
import com.example.app.util.EmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailUtils emailUtils;

    @Value("${app.password-reset.token-expiry-minutes:60}")
    private int tokenExpiryMinutes;

    @Autowired
    public PasswordResetServiceImpl(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            EmailUtils emailUtils) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.emailUtils = emailUtils;
    }

    @Override
    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        // Tìm người dùng theo emails
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với emails: " + email));

        // Vô hiệu hóa các token đặt lại mật khẩu cũ
        List<PasswordResetToken> existingTokens = tokenRepository.findByUser(user);
        existingTokens.forEach(token -> token.setUsed(true));
        tokenRepository.saveAll(existingTokens);

        // Tạo token mới
        PasswordResetToken token = new PasswordResetToken(user, tokenExpiryMinutes);
        tokenRepository.save(token);

        // Tạo đường dẫn đặt lại mật khẩu
        String resetLink = emailUtils.generatePasswordResetLink(token.getToken());

        // Gửi emails đặt lại mật khẩu sử dụng template
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                resetLink
        );
    }

    @Override
    public boolean validatePasswordResetToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElse(null);

        if (resetToken == null || resetToken.isUsed() || resetToken.isExpired()) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public boolean resetPassword(PasswordResetDTO passwordResetDTO) {
        // Xác thực token
        if (!validatePasswordResetToken(passwordResetDTO.getToken())) {
            return false;
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp không
        if (!passwordResetDTO.getNewPassword().equals(passwordResetDTO.getConfirmPassword())) {
            return false;
        }

        // Tìm token và người dùng
        PasswordResetToken resetToken = tokenRepository.findByToken(passwordResetDTO.getToken()).get();
        User user = resetToken.getUser();

        // Cập nhật mật khẩu
        user.setPasswordHash(passwordEncoder.encode(passwordResetDTO.getNewPassword()));
        userRepository.save(user);

        // Đánh dấu token đã được sử dụng
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // Gửi emails thông báo mật khẩu đã được đặt lại sử dụng template thông thường
        String emailBody = "Xin chào " + user.getFirstName() + ",\n\n"
                + "Mật khẩu của bạn đã được đặt lại thành công.\n\n"
                + "Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ với chúng tôi ngay lập tức.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ hỗ trợ";

        emailService.sendEmail(user.getEmail(), "Mật khẩu đã được đặt lại", emailBody);

        return true;
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        // Tìm tất cả các token hết hạn và chưa sử dụng
        List<PasswordResetToken> expiredTokens = tokenRepository.findByUsedFalseAndExpiryDateBefore(LocalDateTime.now());

        // Đánh dấu các token đã hết hạn
        expiredTokens.forEach(token -> token.setUsed(true));
        tokenRepository.saveAll(expiredTokens);
    }
}