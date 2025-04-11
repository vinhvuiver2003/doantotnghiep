package com.example.app.controller;

import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.PaymentDTO;
import com.example.app.entity.Payment;
import com.example.app.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Lấy thông tin thanh toán theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#paymentService.getOrderIdByPaymentId(#id))")
    public ResponseEntity<ResponseWrapper<PaymentDTO>> getPaymentById(@PathVariable Integer id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Payment retrieved successfully", payment));
    }

    /**
     * Lấy thông tin thanh toán theo đơn hàng
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#orderId)")
    public ResponseEntity<ResponseWrapper<PaymentDTO>> getPaymentByOrderId(@PathVariable Integer orderId) {
        PaymentDTO payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ResponseWrapper.success("Payment retrieved successfully", payment));
    }

    /**
     * Tạo URL thanh toán VNPay cho đơn hàng
     */
    @PostMapping("/create-payment-url")
    public ResponseEntity<ResponseWrapper<String>> createPaymentUrl(
            @RequestParam Integer orderId,
            @RequestParam(required = false) String bankCode,
            HttpServletRequest request) {

        String paymentUrl = paymentService.createVnPayPaymentUrl(orderId, bankCode, request);
        return ResponseEntity.ok(ResponseWrapper.success("Payment URL created successfully", paymentUrl));
    }

    /**
     * Xử lý kết quả thanh toán từ VNPay (Callback)
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> vnPayReturn(
            @RequestParam Map<String, String> queryParams,
            HttpServletRequest request) {

        Map<String, String> result = paymentService.processVnPayReturn(queryParams, request);
        boolean isSuccess = Boolean.parseBoolean(result.get("success"));

        if (isSuccess) {
            return ResponseEntity.ok(ResponseWrapper.success("Payment processed successfully", result));
        } else {
            ResponseWrapper<Map<String, String>> response = new ResponseWrapper<>(false, result.get("message"), result);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Xử lý thông báo thanh toán từ VNPay (IPN)
     */
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<String> vnPayIpn(@RequestParam Map<String, String> queryParams) {
        boolean verified = paymentService.verifyVnPayIpn(queryParams);

        if (verified) {
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("FAIL");
        }
    }

    /**
     * Cập nhật trạng thái thanh toán (chỉ ADMIN)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PaymentDTO>> updatePaymentStatus(
            @PathVariable Integer id,
            @RequestParam String status) {

        Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status);
        PaymentDTO updatedPayment = paymentService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(ResponseWrapper.success("Payment status updated successfully", updatedPayment));
    }
}