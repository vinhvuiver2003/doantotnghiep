package com.example.app.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class MistralChatService {

    private static final Logger logger = LoggerFactory.getLogger(MistralChatService.class);

    private final RestTemplate restTemplate;

    @Value("${mistral.api.url}")
    private String apiUrl;

    @Value("${mistral.api.key}")
    private String apiKey;

    @Value("${mistral.model}")
    private String model;

    public MistralChatService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String chat(String message, String context) {
        logger.info("Gửi yêu cầu đến Mistral API...");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> messageObj = new HashMap<>();
        messageObj.put("role", "user");
        messageObj.put("content", context + "\n\nCâu hỏi: " + message);

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("messages", List.of(messageObj));
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 1000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message_response = (Map<String, Object>) choices.get(0).get("message");
                    if (message_response != null && message_response.containsKey("content")) {
                        String responseText = (String) message_response.get("content");
                        logger.info("Nhận phản hồi từ Mistral: {}", responseText);
                        return responseText;
                    }
                }
            }

            logger.error("Lỗi khi gọi Mistral API: {} - {}", response.getStatusCode(), response.getBody());
            return "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này.";

        } catch (Exception e) {
            logger.error("Lỗi khi gọi Mistral API", e);
            return "Đã xảy ra lỗi khi xử lý yêu cầu của bạn: " + e.getMessage();
        }
    }
} 