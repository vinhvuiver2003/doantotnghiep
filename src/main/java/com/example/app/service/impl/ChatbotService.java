package com.example.app.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.math.BigDecimal;

import com.example.app.service.ChatQuestionService;
import com.example.app.service.OrderService;
import com.example.app.dto.OrderDTO;
import com.example.app.dto.OrderItemDTO;
import com.example.app.dto.PagedResponse;
import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import com.example.app.security.CustomUserDetails;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    private final RestTemplate restTemplate;
    private final MistralChatService mistralChatService;
    private final ChatQuestionService chatQuestionService;
    private final OrderService orderService;
    private final UserRepository userRepository;

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

    @Value("${api.user_orders}")
    private String apiUserOrders;

    @Value("${chatbot.system.prompt}")
    private String defaultPrompt;

    public ChatbotService(
        RestTemplate restTemplate, 
        MistralChatService mistralChatService,
        ChatQuestionService chatQuestionService,
        OrderService orderService,
        UserRepository userRepository
    ) {
        this.restTemplate = restTemplate;
        this.mistralChatService = mistralChatService;
        this.chatQuestionService = chatQuestionService;
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Lấy token từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            String token = authentication.getCredentials().toString();
            logger.debug("Tìm thấy token: {}", token);
            headers.setBearerAuth(token);
        } else {
            logger.debug("Không tìm thấy token trong SecurityContext");
        }
        
        return headers;
    }

    public String askChatbot(String message) {
        logger.info("Xử lý yêu cầu chatbot...");

        try {
            chatQuestionService.saveQuestion(message);
        } catch (Exception e) {
            logger.error("Lỗi khi lưu câu hỏi: {}", e.getMessage());
        }
        
        String userName = "Quý khách";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = false;
        final Integer[] userIdRef = new Integer[1]; // Sử dụng mảng để lưu giá trị tham chiếu
        User userEntity = null;
        
        logger.debug("Authentication: {}", authentication);
        if (authentication != null) {
            logger.debug("Authentication principal: {}", authentication.getPrincipal());
            logger.debug("Authentication credentials: {}", authentication.getCredentials());
            logger.debug("Authentication authenticated: {}", authentication.isAuthenticated());
            logger.debug("Authentication principal class: {}", 
                       authentication.getPrincipal() != null ? 
                       authentication.getPrincipal().getClass().getName() : "null");
        }
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() != null && 
            !"anonymousUser".equals(authentication.getPrincipal().toString())) {
            try {
                if (authentication.getPrincipal() instanceof User) {
                    userEntity = (User) authentication.getPrincipal();
                    isAuthenticated = true;
                    userIdRef[0] = userEntity.getId();
                    logger.debug("Principal là User: ID={}, username={}", userIdRef[0], userEntity.getUsername());
                } else if (authentication.getPrincipal() instanceof CustomUserDetails) {
                    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                    isAuthenticated = true;
                    userIdRef[0] = userDetails.getId();
                    logger.debug("Principal là CustomUserDetails: ID={}, username={}", userIdRef[0], userDetails.getUsername());
                    
                    userEntity = userRepository.findById(userIdRef[0])
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy User với ID: " + userIdRef[0]));
                } else if (authentication.getPrincipal() instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    String username = userDetails.getUsername();
                    logger.debug("Principal là UserDetails: username={}", username);
                    
                    userEntity = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy User với username: " + username));
                    isAuthenticated = true;
                    userIdRef[0] = userEntity.getId();
                } else if (authentication.getPrincipal() instanceof String) {
                    String username = (String) authentication.getPrincipal();
                    if (!"anonymousUser".equals(username)) {
                        logger.debug("Principal là String username: {}", username);
                        Optional<User> userOpt = userRepository.findByUsername(username);
                        if (userOpt.isPresent()) {
                            userEntity = userOpt.get();
                            isAuthenticated = true;
                            userIdRef[0] = userEntity.getId();
                        }
                    }
                }
                
                if (userEntity != null) {
                    if (userEntity.getFirstName() != null && !userEntity.getFirstName().isEmpty()) {
                        userName = userEntity.getFirstName();
                        // Thêm họ nếu có
                        if (userEntity.getLastName() != null && !userEntity.getLastName().isEmpty()) {
                            userName = userEntity.getFirstName() + " " + userEntity.getLastName();
                        }
                    } else if (userEntity.getUsername() != null && !userEntity.getUsername().isEmpty()) {
                        userName = userEntity.getUsername();
                    }
                    logger.debug("Đã xác định được tên người dùng: {}", userName);
                }
            } catch (Exception e) {
                logger.error("Lỗi khi lấy thông tin người dùng: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("Người dùng chưa đăng nhập hoặc authentication không hợp lệ");
        }

        String lowerMessage = message.toLowerCase();
        boolean isOrderQuery = lowerMessage.contains("đơn hàng") || 
                               lowerMessage.contains("đơn đặt hàng") || 
                               lowerMessage.contains("lịch sử đơn hàng") || 
                               lowerMessage.contains("order") || 
                               lowerMessage.contains("trạng thái đơn") ||
                               (lowerMessage.contains("đơn") && lowerMessage.contains("mua"));

        if (isOrderQuery) {
            if (isAuthenticated && userIdRef[0] != null) {
                try {
                    PagedResponse<OrderDTO> userOrders = orderService.getOrdersByUser(userIdRef[0], 0, 10);
                    
                    if (userOrders != null && !userOrders.getContent().isEmpty()) {
                        StringBuilder response = new StringBuilder();
                        response.append("Xin chào ").append(userName).append("! Đây là thông tin đơn hàng của bạn:\n\n");
                        response.append("Tổng cộng: ").append(userOrders.getTotalElements()).append(" đơn hàng.\n\n");
                        
                        for (OrderDTO order : userOrders.getContent()) {
                            response.append("🛒 ĐƠN HÀNG #").append(order.getId()).append("\n");
                            response.append("━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                            response.append("📅 Ngày đặt: ").append(order.getCreatedAt()).append("\n");
                            response.append("📌 Trạng thái: ").append(getOrderStatusVietnamese(order.getOrderStatus())).append("\n");
                            response.append("💰 Tổng tiền: ").append(formatCurrency(order.getFinalAmount())).append("\n");
                            
                            if (order.getItems() != null && !order.getItems().isEmpty()) {
                                response.append("\n📋 Sản phẩm:\n");
                                for (OrderItemDTO item : order.getItems()) {
                                    response.append("   • ").append(item.getProductName())
                                          .append(" (SL: ").append(item.getQuantity())
                                          .append(", Đơn giá: ").append(formatCurrency(item.getUnitPrice())).append(")\n");
                                }
                            }
                            
                            if (order.getDelivery() != null) {
                                response.append("\n🚚 Thông tin giao hàng:\n");
                                response.append("   • Địa chỉ: ").append(order.getDelivery().getShippingAddress()).append("\n");
                                response.append("   • Phương thức: ").append(order.getDelivery().getShippingMethod()).append("\n");
                                if (order.getDelivery().getTrackingNumber() != null && !order.getDelivery().getTrackingNumber().isEmpty()) {
                                    response.append("   • Mã vận đơn: ").append(order.getDelivery().getTrackingNumber()).append("\n");
                                }
                            }
                            
                            response.append("\n\n");
                        }
                        
                        response.append("Cảm ơn ").append(userName).append(" đã sử dụng dịch vụ của chúng tôi! Bạn có thể xem chi tiết đơn hàng tại trang 'Đơn hàng của tôi' trên website hoặc ứng dụng.");
                        
                        return response.toString();
                    } else {
                        return "Xin chào " + userName + "! Hiện tại bạn chưa có đơn hàng nào. Hãy khám phá các sản phẩm của chúng tôi và đặt hàng ngay!";
                    }
                } catch (Exception e) {
                    logger.error("Lỗi khi lấy thông tin đơn hàng: {}", e.getMessage());
                }
            } else {
                return "Xin chào! Để xem thông tin đơn hàng, bạn cần đăng nhập vào tài khoản của mình. Vui lòng đăng nhập và thử lại.";
            }
        }

        StringBuilder context = new StringBuilder(defaultPrompt);
        
        if (isAuthenticated) {
            context.append("\n\nThông tin người dùng: Tên: ").append(userName);
        }
        
        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> productsResponse = restTemplate.exchange(
                apiproducts, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (productsResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nDữ liệu sản phẩm: ").append(productsResponse.getBody());
            }

            ResponseEntity<String> promotionsResponse = restTemplate.exchange(
                apipromotions, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (promotionsResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nKhuyến mãi: ").append(promotionsResponse.getBody());
            }

            ResponseEntity<String> topRatedResponse = restTemplate.exchange(
                apiTopRated, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (topRatedResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nSản phẩm đánh giá cao: ").append(topRatedResponse.getBody());
            }

            ResponseEntity<String> newArrivalsResponse = restTemplate.exchange(
                apiNewArrivals, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (newArrivalsResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nSản phẩm mới: ").append(newArrivalsResponse.getBody());
            }

            ResponseEntity<String> bestSellingResponse = restTemplate.exchange(
                apiBestSelling, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (bestSellingResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nSản phẩm bán chạy: ").append(bestSellingResponse.getBody());
            }

            ResponseEntity<String> categoriesResponse = restTemplate.exchange(
                apiCategories, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (categoriesResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nDanh mục sản phẩm: ").append(categoriesResponse.getBody());
            }

            ResponseEntity<String> brandsResponse = restTemplate.exchange(
                apiBrands, 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            if (brandsResponse.getStatusCode() == HttpStatus.OK) {
                context.append("\n\nThương hiệu: ").append(brandsResponse.getBody());
            }

            if (isAuthenticated && userIdRef[0] != null) {
                try {
                    PagedResponse<OrderDTO> userOrders = orderService.getOrdersByUser(userIdRef[0], 0, 10);
                    
                    context.append("\n\nĐơn hàng của người dùng: ");
                    
                    if (userOrders != null && !userOrders.getContent().isEmpty()) {
                        context.append("Có ").append(userOrders.getTotalElements()).append(" đơn hàng.\n");
                        
                        for (OrderDTO order : userOrders.getContent()) {
                            context.append("- Mã đơn hàng: ").append(order.getId())
                                  .append(", Trạng thái: ").append(order.getOrderStatus())
                                  .append(", Ngày đặt: ").append(order.getCreatedAt())
                                  .append(", Tổng tiền: ").append(order.getFinalAmount()).append("\n");
                                  
                            if (order.getItems() != null && !order.getItems().isEmpty()) {
                                context.append("  Sản phẩm: ");
                                order.getItems().forEach(item -> 
                                    context.append("\n    + ").append(item.getProductName())
                                          .append(" (SL: ").append(item.getQuantity())
                                          .append(", Giá: ").append(item.getUnitPrice()).append(")")
                                );
                                context.append("\n");
                            }
                        }
                    } else {
                        context.append("Không có đơn hàng nào.\n");
                    }
                } catch (Exception e) {
                    logger.error("Lỗi khi lấy thông tin đơn hàng: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.warn("Lỗi khi thu thập dữ liệu context: {}", e.getMessage());
        }

        String finalContext = context.toString();
        if (isAuthenticated) {
            finalContext += "\n\nKhi trả lời người dùng, hãy sử dụng tên của họ là '" + userName + "' và xưng hô thân thiện.";
        }
        
        String response = mistralChatService.chat(message, finalContext);
        
        if (isAuthenticated && !response.contains(userName)) {
            if (response.startsWith("Xin chào") || response.startsWith("Chào")) {
            } else {
                response = "Xin chào " + userName + "! " + response;
            }
        }
        
        return response;
    }


    private String getOrderStatusVietnamese(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "Chờ xác nhận";
            case "processing":
                return "Đang xử lý";
            case "shipped":
                return "Đang giao hàng";
            case "delivered":
                return "Đã giao hàng";
            case "cancelled":
                return "Đã hủy";
            case "refunded":
                return "Đã hoàn tiền";
            default:
                return status;
        }
    }


    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        return String.format("%,.0f ₫", amount);
    }
}
