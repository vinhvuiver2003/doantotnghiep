package com.example.app.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    private final RestTemplate restTemplate;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${api.products}")
    private String apiproducts;

    @Value("${api.promotions}")
    private String apipromotions;

    @Value("${api.top_rated}")
    private String apiTopRated = "http://localhost:8080/api/products/top-rated?limit=5";

    @Value("${api.new_arrivals}")
    private String apiNewArrivals = "http://localhost:8080/api/products/new-arrivals?limit=5";

    @Value("${api.best_selling}")
    private String apiBestSelling = "http://localhost:8080/api/products/best-selling?limit=5";

    @Value("${chatbot.system.prompt}")
    private String defaultPrompt;

    public ChatbotService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String askChatbot(String message) {
        logger.info("Đang gọi API Gemini...");
        String fullUrl = apiUrl + "?key=" + apiKey;

        String productsData = "";
        String promotionsData = "";
        String topRatedData = "";
        String newArrivalsData = "";
        String bestSellingData = "";
        try {
            ResponseEntity<String> extraDataResponse = restTemplate.getForEntity(apiproducts, String.class);
            if (extraDataResponse.getStatusCode() == HttpStatus.OK) {
                productsData = extraDataResponse.getBody();
                logger.info("Dữ liệu bổ sung: {}", productsData);
            } else {
                logger.warn("Không thể lấy dữ liệu bổ sung. Mã lỗi: {}", extraDataResponse.getStatusCode());
            }
        } catch (Exception ex) {
            logger.warn("Lỗi khi lấy dữ liệu bổ sung: {}", ex.getMessage());
        }
        
        try {
            ResponseEntity<String> topRatedResponse = restTemplate.getForEntity(apiTopRated, String.class);
            if (topRatedResponse.getStatusCode() == HttpStatus.OK) {
                topRatedData = topRatedResponse.getBody();
                logger.info("Dữ liệu sản phẩm đánh giá cao: {}", topRatedData);
            } else {
                logger.warn("Không thể lấy dữ liệu đánh giá cao. Mã lỗi: {}", topRatedResponse.getStatusCode());
            }
        } catch (Exception ex) {
            logger.warn("Lỗi khi lấy dữ liệu đánh giá cao: {}", ex.getMessage());
        }
        
        try {
            ResponseEntity<String> newArrivalsResponse = restTemplate.getForEntity(apiNewArrivals, String.class);
            if (newArrivalsResponse.getStatusCode() == HttpStatus.OK) {
                newArrivalsData = newArrivalsResponse.getBody();
                logger.info("Dữ liệu sản phẩm mới: {}", newArrivalsData);
            } else {
                logger.warn("Không thể lấy dữ liệu sản phẩm mới. Mã lỗi: {}", newArrivalsResponse.getStatusCode());
            }
        } catch (Exception ex) {
            logger.warn("Lỗi khi lấy dữ liệu sản phẩm mới: {}", ex.getMessage());
        }
        
        try {
            ResponseEntity<String> bestSellingResponse = restTemplate.getForEntity(apiBestSelling, String.class);
            if (bestSellingResponse.getStatusCode() == HttpStatus.OK) {
                bestSellingData = bestSellingResponse.getBody();
                logger.info("Dữ liệu sản phẩm bán chạy nhất: {}", bestSellingData);
            } else {
                logger.warn("Không thể lấy dữ liệu sản phẩm bán chạy. Mã lỗi: {}", bestSellingResponse.getStatusCode());
            }
        } catch (Exception ex) {
            logger.warn("Lỗi khi lấy dữ liệu sản phẩm bán chạy: {}", ex.getMessage());
        }
        
        try {
            ResponseEntity<String> userResponse = restTemplate.getForEntity(apipromotions, String.class);
            if (userResponse.getStatusCode() == HttpStatus.OK) {
                promotionsData = userResponse.getBody();
                logger.info("Dữ liệu người dùng: {}", promotionsData);
            } else {
                logger.warn("Không thể lấy dữ liệu người dùng. Mã lỗi: {}", userResponse.getStatusCode());
            }
        } catch (Exception ex) {
            logger.warn("Lỗi khi lấy dữ liệu người dùng: {}", ex.getMessage());
        }
        // 🔧 Thêm dữ liệu bổ sung vào prompt
        String fullMessage = defaultPrompt + 
                "\n\nDữ liệu bổ sung: thông tin các sản phẩm: " + productsData + 
                "\ncác thông tin khuyến mãi: " + promotionsData + 
                "\nsản phẩm đánh giá cao nhất: " + topRatedData + 
                "\nsản phẩm mới nhất: " + newArrivalsData + 
                "\nsản phẩm bán chạy nhất: " + bestSellingData +
                "\n\nCâu hỏi người dùng: " + message;


        // Tạo payload theo định dạng Gemini
        Map<String, Object> userPart = new HashMap<>();
        userPart.put("text", fullMessage);

        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");
        userContent.put("parts", List.of(userPart));

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", List.of(userContent));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept-Charset", "UTF-8"); // Đảm bảo sử dụng mã hóa UTF-8


        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            logger.debug("Gửi POST đến: {}", fullUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(fullUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map body = response.getBody();

                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");

                    if (content != null) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty() && parts.get(0).containsKey("text")) {
                            String responseText = (String) parts.get(0).get("text");
                            logger.info("Phản hồi chatbot: {}", responseText);
                            return responseText;
                        }
                    }
                }

                logger.warn("Không tìm thấy nội dung phản hồi.");
                return "Không có phản hồi từ chatbot.";
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.error("Lỗi 401: Unauthorized – API key sai hoặc hết hạn.");
                return "Lỗi: API key không hợp lệ hoặc hết hạn.";
            } else {
                logger.error("Lỗi khi gọi API Gemini: {} - {}", response.getStatusCode(), response.getBody());
                return "Lỗi khi gọi API Gemini: " + response.getStatusCode();
            }

        } catch (Exception e) {
            logger.error("Exception khi gọi API Gemini", e);
            return "Đã xảy ra lỗi: " + e.getMessage();
        }
    }
}
