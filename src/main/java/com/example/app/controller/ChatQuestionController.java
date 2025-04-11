package com.example.app.controller;

import com.example.app.entity.ChatQuestion;
import com.example.app.service.ChatQuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/chat-questions")
@PreAuthorize("hasRole('ADMIN')")
public class ChatQuestionController {

    private final ChatQuestionService chatQuestionService;

    public ChatQuestionController(ChatQuestionService chatQuestionService) {
        this.chatQuestionService = chatQuestionService;
    }

    @GetMapping("/frequent")
    public ResponseEntity<List<ChatQuestion>> getFrequentQuestions() {
        return ResponseEntity.ok(chatQuestionService.getFrequentQuestions());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ChatQuestion>> getRecentFrequentQuestions(
        @RequestParam(defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(chatQuestionService.getRecentFrequentQuestions(days));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getQuestionStats() {
        List<ChatQuestion> allQuestions = chatQuestionService.getFrequentQuestions();
        List<ChatQuestion> recentQuestions = chatQuestionService.getRecentFrequentQuestions(7);

        return ResponseEntity.ok(Map.of(
            "totalQuestions", allQuestions.size(),
            "topQuestions", allQuestions.stream().limit(10).toList(),
            "recentTopQuestions", recentQuestions.stream().limit(10).toList()
        ));
    }
} 