package com.example.app.service;

import com.example.app.dto.PaymentDTO;
import com.example.app.entity.Payment;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface PaymentService {
    PaymentDTO getPaymentById(Integer id);

    PaymentDTO getPaymentByOrderId(Integer orderId);

    /**
     * @deprecated Sử dụng phương thức createSePayPaymentUrl thay thế
     */
    @Deprecated
    String createVnPayPaymentUrl(Integer orderId, String bankCode, HttpServletRequest request);

    /**
     * @deprecated Sử dụng phương thức processSePayCallback thay thế
     */
    @Deprecated
    Map<String, String> processVnPayReturn(Map<String, String> queryParams, HttpServletRequest request);

    /**
     * @deprecated Sử dụng phương thức verifySePayIpn thay thế
     */
    @Deprecated
    boolean verifyVnPayIpn(Map<String, String> queryParams);

    /**
     * Tạo URL thanh toán SePay cho đơn hàng
     * @param orderId ID đơn hàng
     * @param paymentMethod Phương thức thanh toán (ATM, CC, EWALLET, ...)
     * @param request HTTP request
     * @return URL thanh toán
     */
    String createSePayPaymentUrl(Integer orderId, String paymentMethod, HttpServletRequest request);

    /**
     * Xử lý kết quả thanh toán từ SePay
     * @param queryParams Các tham số trả về từ SePay
     * @param request HTTP request
     * @return Kết quả xử lý thanh toán
     */
    Map<String, String> processSePayCallback(Map<String, String> queryParams, HttpServletRequest request);

    /**
     * Xác thực và xử lý thông báo thanh toán từ SePay (IPN)
     * @param queryParams Các tham số nhận được từ SePay IPN
     * @return true nếu xác thực thành công, false nếu ngược lại
     */
    boolean verifySePayIpn(Map<String, String> queryParams);

    PaymentDTO updatePaymentStatus(Integer id, Payment.PaymentStatus status);

    Integer getOrderIdByPaymentId(Integer paymentId);
}