package com.example.app.repository;

import com.example.app.entity.User;
import com.example.app.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findByToken(String token);
    List<VerificationToken> findByUser(User user);
    void deleteByUser(User user);
}