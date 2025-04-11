package com.example.app.service;

import com.example.app.entity.ChatQuestion;
import com.example.app.repository.ChatQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatQuestionService {
    
    private final ChatQuestionRepository chatQuestionRepository;
    
    public ChatQuestionService(ChatQuestionRepository chatQuestionRepository) {
        this.chatQuestionRepository = chatQuestionRepository;
    }
    
    @Transactional
    public void saveQuestion(String question) {
        ChatQuestion existingQuestion = chatQuestionRepository.findByQuestion(question);
        
        if (existingQuestion != null) {
            existingQuestion.setFrequency(existingQuestion.getFrequency() + 1);
            chatQuestionRepository.save(existingQuestion);
        } else {
            ChatQuestion newQuestion = new ChatQuestion();
            newQuestion.setQuestion(question);
            chatQuestionRepository.save(newQuestion);
        }
    }
    
    public List<ChatQuestion> getFrequentQuestions() {
        return chatQuestionRepository.findAllOrderByFrequencyAndLastAskedAt();
    }
    
    public List<ChatQuestion> getRecentFrequentQuestions(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return chatQuestionRepository.findRecentFrequentQuestions(startDate);
    }
} 