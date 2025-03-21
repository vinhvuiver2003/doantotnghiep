package com.example.app.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class EmailUtils {

    @Value("${app.email.verification-base-url}")
    private String verificationBaseUrl;

    @Value("${app.email.password-reset-base-url}")
    private String passwordResetBaseUrl;

    @Value("${app.email.verification-expiry-hours}")
    private int verificationExpiryHours;

    @Value("${app.password-reset.token-expiry-minutes}")
    private int passwordResetExpiryMinutes;

    /**
     * Tạo đường dẫn xác thực emails
     *
     * @param token Token xác thực
     * @return Đường dẫn xác thực đầy đủ
     */
    public String generateVerificationLink(String token) {
        return verificationBaseUrl + "?token=" + token;
    }

    /**
     * Tạo đường dẫn đặt lại mật khẩu
     *
     * @param token Token đặt lại mật khẩu
     * @return Đường dẫn đặt lại mật khẩu đầy đủ
     */
    public String generatePasswordResetLink(String token) {
        return passwordResetBaseUrl + "?token=" + token;
    }

    /**
     * Tạo token ngẫu nhiên
     *
     * @return Token ngẫu nhiên
     */
    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Tính thời gian hết hạn cho token xác thực
     *
     * @return Thời gian hết hạn
     */
    public LocalDateTime calculateVerificationExpiry() {
        return LocalDateTime.now().plusHours(verificationExpiryHours);
    }

    /**
     * Tính thời gian hết hạn cho token đặt lại mật khẩu
     *
     * @return Thời gian hết hạn
     */
    public LocalDateTime calculatePasswordResetExpiry() {
        return LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes);
    }
}