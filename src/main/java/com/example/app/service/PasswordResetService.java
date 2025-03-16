package com.example.app.service;

import com.example.app.dto.PasswordResetRequestDTO;
import com.example.app.dto.PasswordResetDTO;

public interface PasswordResetService {
    void createPasswordResetTokenForUser(String email);
    boolean validatePasswordResetToken(String token);
    boolean resetPassword(PasswordResetDTO passwordResetDTO);
    void cleanupExpiredTokens();
}