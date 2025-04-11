package com.example.app.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.*;

import com.example.app.service.ChatQuestionService;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    private final RestTemplate restTemplate;
    private final MistralChatService mistralChatService;
    private final ChatQuestionService chatQuestionService;

    @Value("${api.products}")
    private String apiproducts;

    @Value("${api.promotions}")
    private String apipromotions;

    @Value("${api.top_rated}")
    private String apiTopRated;

    @Value("${api.new_arrivals}")
    private String apiNewArrivals;

    @Value("${api.best_selling}")
    private String apiBestSelling;

    @Value("${api.categories}")
    private String apiCategories;

    @Value("${api.brands}")
    private String apiBrands;

    @Value("${chatbot.system.prompt}")
    private String defaultPrompt;

    public ChatbotService(
        RestTemplate restTemplate, 
        MistralChatService mistralChatService,
        ChatQuestionService chatQuestionService
    ) {
        this.restTemplate = restTemplate;
        this.mistralChatService = mistralChatService;
        this.chatQuestionService = chatQuestionService;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Lấy token từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            String token = authentication.getCredentials().toString();
            headers.setBearerAuth(token);
        }
        
        return headers;
    }

    public String askChatbot(String message) {
        logger.info("Xử lý yêu cầu chatbot...");

        // Lưu câu hỏi của người dùng
        try {
            chatQuestionService.saveQuestion(message);
        } catch (Exception e) {
            logger.error("Lỗi khi lưu câu hỏi: {}", e.getMessage());
        }

        // Thu thập dữ liệu context
        StringBuilder context = new StringBuilder(defaultPrompt);
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Thêm dữ liệu sản phẩm
            ResponseEntity<String> productsResponse = restTemplate.exchange(
                apiproducts, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (productsResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nDữ liệu sản phẩm: ").append(productsResponse.getBody());
            }

            // Thêm dữ liệu khuyến mãi
            ResponseEntity<String> promotionsResponse = restTemplate.exchange(
                apipromotions, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (promotionsResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nKhuyến mãi: ").append(promotionsResponse.getBody());
            }

            // Thêm sản phẩm đánh giá cao
            ResponseEntity<String> topRatedResponse = restTemplate.exchange(
                apiTopRated, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (topRatedResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nSản phẩm đánh giá cao: ").append(topRatedResponse.getBody());
            }

            // Thêm sản phẩm mới
            ResponseEntity<String> newArrivalsResponse = restTemplate.exchange(
                apiNewArrivals, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (newArrivalsResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nSản phẩm mới: ").append(newArrivalsResponse.getBody());
            }

            // Thêm sản phẩm bán chạy
            ResponseEntity<String> bestSellingResponse = restTemplate.exchange(
                apiBestSelling, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (bestSellingResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nSản phẩm bán chạy: ").append(bestSellingResponse.getBody());
            }

            // Thêm danh mục
            ResponseEntity<String> categoriesResponse = restTemplate.exchange(
                apiCategories, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (categoriesResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nDanh mục sản phẩm: ").append(categoriesResponse.getBody());
            }

            // Thêm thương hiệu
            ResponseEntity<String> brandsResponse = restTemplate.exchange(
                apiBrands, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (brandsResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nThương hiệu: ").append(brandsResponse.getBody());
            }

        } catch (Exception e) {
            logger.warn("Lỗi khi thu thập dữ liệu context: {}", e.getMessage());
        }

        // Gọi Mistral API với context đã thu thập
        return mistralChatService.chat(message, context.toString());
    }
}
