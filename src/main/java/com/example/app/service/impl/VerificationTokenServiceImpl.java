package com.example.app.service.impl;

import com.example.app.entity.User;
import com.example.app.entity.VerificationToken;
import com.example.app.repository.VerificationTokenRepository;
import com.example.app.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;

    @Value("${app.email.verification-expiry-hours}")
    private int defaultExpiryHours;

    @Autowired
    public VerificationTokenServiceImpl(VerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    @Transactional
    public VerificationToken createVerificationToken(User user, int expiryHours) {
        List<VerificationToken> existingTokens = tokenRepository.findByUser(user);
        existingTokens.forEach(token -> token.setUsed(true));
        tokenRepository.saveAll(existingTokens);

        VerificationToken token = new VerificationToken(user, expiryHours);
        return tokenRepository.save(token);
    }

    @Override
    public VerificationToken findByToken(String token) {
        return tokenRepository.findByToken(token).orElse(null);
    }

    @Override
    public boolean validateToken(String token) {
        VerificationToken verificationToken = findByToken(token);
        return verificationToken != null && !verificationToken.isUsed() && !verificationToken.isExpired();
    }

    @Override
    @Transactional
    public void markTokenAsUsed(String token) {
        VerificationToken verificationToken = findByToken(token);
        if (verificationToken != null) {
            verificationToken.setUsed(true);
            tokenRepository.save(verificationToken);
        }
    }

    @Override
    @Transactional
    public void deleteTokensByUser(User user) {
        tokenRepository.deleteByUser(user);
    }
}