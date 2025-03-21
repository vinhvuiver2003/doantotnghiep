package com.example.app.service;

import com.example.app.entity.User;
import com.example.app.entity.VerificationToken;

import java.time.LocalDateTime;

public interface VerificationTokenService {

    VerificationToken createVerificationToken(User user, int expiryHours);
    VerificationToken findByToken(String token);
    boolean validateToken(String token);
    void markTokenAsUsed(String token);
    void deleteTokensByUser(User user);
}