package com.example.app.service;

import com.example.app.dto.PaymentDTO;
import com.example.app.entity.Payment;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface PaymentService {
    PaymentDTO getPaymentById(Integer id);

    PaymentDTO getPaymentByOrderId(Integer orderId);

    String createMomoPaymentUrl(Integer orderId, HttpServletRequest request);

    Map<String, String> processMomoReturn(Map<String, String> queryParams, HttpServletRequest request);

    boolean verifyMomoIpn(Map<String, String> queryParams);

    PaymentDTO updatePaymentStatus(Integer id, Payment.PaymentStatus status);

    Integer getOrderIdByPaymentId(Integer paymentId);
}