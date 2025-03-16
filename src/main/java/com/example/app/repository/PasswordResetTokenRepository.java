package com.example.app.repository;

import com.example.app.entity.PasswordResetToken;
import com.example.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findByUser(User user);
    void deleteByExpiryDateBefore(LocalDateTime now);
    List<PasswordResetToken> findByUsedFalseAndExpiryDateBefore(LocalDateTime now);
}