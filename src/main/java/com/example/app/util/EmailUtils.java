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


    public String generateVerificationLink(String token) {
        return verificationBaseUrl + "?token=" + token;
    }


    public String generatePasswordResetLink(String token) {
        return passwordResetBaseUrl + "?token=" + token;
    }


    public String generateToken() {
        return UUID.randomUUID().toString();
    }


    public LocalDateTime calculateVerificationExpiry() {
        return LocalDateTime.now().plusHours(verificationExpiryHours);
    }


    public LocalDateTime calculatePasswordResetExpiry() {
        return LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes);
    }
}