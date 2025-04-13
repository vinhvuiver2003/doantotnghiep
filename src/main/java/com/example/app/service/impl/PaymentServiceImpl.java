package com.example.app.service.impl;

import com.example.app.dto.PaymentDTO;
import com.example.app.entity.Order;
import com.example.app.entity.Payment;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.OrderRepository;
import com.example.app.repository.PaymentRepository;
import com.example.app.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.example.app.config.MomoConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    
    @Value("${app.base-url}")
    private String appBaseUrl;

    private final MomoConfig momoConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            MomoConfig momoConfig,
            ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.momoConfig = momoConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentDTO getPaymentById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        return convertToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByOrderId(Integer orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order id: " + orderId));

        return convertToDTO(payment);
    }

    @Override
    @Transactional
    public PaymentDTO updatePaymentStatus(Integer id, Payment.PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        payment.setStatus(status);

        if (status == Payment.PaymentStatus.completed && payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());

            Order order = payment.getOrder();
            order.setOrderStatus(Order.OrderStatus.processing);
            orderRepository.save(order);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return convertToDTO(updatedPayment);
    }

    @Override
    public Integer getOrderIdByPaymentId(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        return payment.getOrder().getId();
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setStatus(payment.getStatus().name());
        dto.setAmount(payment.getAmount());
        dto.setBankTransferCode(payment.getBankTransferCode());
        dto.setBankAccount(payment.getBankAccount());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setCreatedAt(payment.getCreatedAt());

        return dto;
    }

    @Override
    public String createMomoPaymentUrl(Integer orderId, HttpServletRequest request) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
            
            Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);
            Payment payment;
            
            if (existingPayment.isPresent()) {
                payment = existingPayment.get();
                if (payment.getStatus() == Payment.PaymentStatus.completed) {
                    throw new IllegalStateException("Payment for this order has already been completed");
                }
            } else {
                payment = new Payment();
                payment.setOrder(order);
                payment.setStatus(Payment.PaymentStatus.pending);
                payment.setAmount(order.getFinalAmount());
                payment = paymentRepository.save(payment);
            }
            
            String requestId = UUID.randomUUID().toString();
            String orderId2 = orderId + "-" + System.currentTimeMillis();
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("partnerCode", momoConfig.getPartnerCode());
            requestData.put("accessKey", momoConfig.getAccessKey());
            requestData.put("requestId", requestId);
            requestData.put("amount", order.getFinalAmount().intValue());
            requestData.put("orderId", orderId2);
            requestData.put("orderInfo", "Thanh toan don hang " + orderId);
            requestData.put("returnUrl", appBaseUrl + momoConfig.getReturnUrl());
            requestData.put("notifyUrl", appBaseUrl + momoConfig.getNotifyUrl());
            requestData.put("requestType", "captureWallet");
            requestData.put("extraData", "");
            
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + requestData.get("amount") +
                    "&extraData=" + requestData.get("extraData") +
                    "&ipnUrl=" + appBaseUrl + momoConfig.getNotifyUrl() +
                    "&orderId=" + orderId2 +
                    "&orderInfo=" + requestData.get("orderInfo") +
                    "&partnerCode=" + momoConfig.getPartnerCode() +
                    "&redirectUrl=" + appBaseUrl + momoConfig.getReturnUrl() +
                    "&requestId=" + requestId +
                    "&requestType=" + requestData.get("requestType");
            
            String signature = hmacSHA256(rawSignature, momoConfig.getSecretKey());
            requestData.put("signature", signature);
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(momoConfig.getEndpoint()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestData)))
                    .build();
            
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            Map<String, Object> responseData = objectMapper.readValue(response.body(), Map.class);
            
            if (responseData.containsKey("payUrl")) {
                return (String) responseData.get("payUrl");
            } else {
                throw new RuntimeException("Failed to get payment URL from MoMo: " + responseData.get("message"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating MoMo payment URL", e);
        }
    }
    
    @Override
    public Map<String, String> processMomoReturn(Map<String, String> queryParams, HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        
        try {
            String signature = queryParams.get("signature");
            if (signature == null) {
                result.put("success", "false");
                result.put("message", "Invalid signature");
                return result;
            }
            
            String resultCode = queryParams.get("resultCode");
            String orderId = queryParams.get("orderId");
            String amount = queryParams.get("amount");
            
            String[] orderRef = orderId.split("-");
            Integer orderId1 = Integer.parseInt(orderRef[0]);
            
            Order order = orderRepository.findById(orderId1)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId1));
            
            Payment payment = paymentRepository.findByOrderId(orderId1)
                    .orElseGet(() -> {
                        Payment newPayment = new Payment();
                        newPayment.setOrder(order);
                        newPayment.setAmount(order.getFinalAmount());
                        return newPayment;
                    });
            
            BigDecimal paymentAmount = new BigDecimal(amount);
            if (paymentAmount.compareTo(order.getFinalAmount()) != 0) {
                result.put("success", "false");
                result.put("message", "Invalid amount");
                payment.setStatus(Payment.PaymentStatus.failed);
                paymentRepository.save(payment);
                return result;
            }
            
            if ("0".equals(resultCode)) {
                payment.setStatus(Payment.PaymentStatus.completed);
                payment.setPaymentDate(LocalDateTime.now());
                
                order.setOrderStatus(Order.OrderStatus.processing);
                orderRepository.save(order);
                
                result.put("success", "true");
                result.put("message", "Payment successful");
            } else {
                payment.setStatus(Payment.PaymentStatus.failed);
                
                result.put("success", "false");
                result.put("message", "Payment failed: " + resultCode);
            }
            
            payment.setBankTransferCode(queryParams.get("transId"));
            payment.setBankAccount("MoMo Wallet");
            paymentRepository.save(payment);
            
            result.put("orderId", order.getId().toString());
            result.put("amount", paymentAmount.toString());
            result.put("paymentDate", payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "");
            result.put("paymentMethod", "MoMo");
            result.put("transactionNo", queryParams.get("transId"));
            
            return result;
        } catch (Exception e) {
            result.put("success", "false");
            result.put("message", "Error processing payment: " + e.getMessage());
            return result;
        }
    }
    
    @Override
    public boolean verifyMomoIpn(Map<String, String> queryParams) {
        try {
            String signature = queryParams.get("signature");
            if (signature == null) {
                return false;
            }
            
            String resultCode = queryParams.get("resultCode");
            String orderId = queryParams.get("orderId");
            String amount = queryParams.get("amount");
            
            String[] orderRef = orderId.split("-");
            if (orderRef.length < 1) {
                return false;
            }
            
            Integer orderId1 = Integer.parseInt(orderRef[0]);
            
            Optional<Order> orderOpt = orderRepository.findById(orderId1);
            if (!orderOpt.isPresent()) {
                return false;
            }
            
            Order order = orderOpt.get();
            
            BigDecimal paymentAmount = new BigDecimal(amount);
            if (paymentAmount.compareTo(order.getFinalAmount()) != 0) {
                return false;
            }
            
            if ("0".equals(resultCode)) {
                Payment payment = paymentRepository.findByOrderId(orderId1)
                        .orElseGet(() -> {
                            Payment newPayment = new Payment();
                            newPayment.setOrder(order);
                            newPayment.setAmount(order.getFinalAmount());
                            return newPayment;
                        });
                
                payment.setStatus(Payment.PaymentStatus.completed);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setBankTransferCode(queryParams.get("transId"));
                payment.setBankAccount("MoMo Wallet");
                paymentRepository.save(payment);
                
                order.setOrderStatus(Order.OrderStatus.processing);
                orderRepository.save(order);
                
                return true;
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String hmacSHA256(String data, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            
            byte[] hash = sha256_HMAC.doFinal(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HMAC-SHA256", e);
        }
    }
}