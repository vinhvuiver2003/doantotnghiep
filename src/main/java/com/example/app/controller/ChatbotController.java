package com.example.app.controller;

import com.example.app.dto.ChatRequest;
import com.example.app.dto.ChatbotResponse;
import com.example.app.service.impl.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public ResponseEntity<ChatbotResponse> ask(@RequestBody ChatRequest request) {
        logger.info("Nhận yêu cầu hỏi chatbot: {}", request.getMessage());
        String chatbotResponse = chatbotService.askChatbot(request.getMessage());
        ChatbotResponse response = new ChatbotResponse(chatbotResponse);
        logger.info("Phản hồi của chatbot: {}", response.getResponse());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}