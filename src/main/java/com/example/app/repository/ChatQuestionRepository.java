package com.example.app.repository;

import com.example.app.entity.ChatQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface ChatQuestionRepository extends JpaRepository<ChatQuestion, Long> {
    
    ChatQuestion findByQuestion(String question);
    
    @Query("SELECT c FROM ChatQuestion c ORDER BY c.frequency DESC, c.lastAskedAt DESC")
    List<ChatQuestion> findAllOrderByFrequencyAndLastAskedAt();
    
    @Query("SELECT c FROM ChatQuestion c WHERE c.lastAskedAt >= :startDate ORDER BY c.frequency DESC")
    List<ChatQuestion> findRecentFrequentQuestions(LocalDateTime startDate);
} 