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


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#paymentService.getOrderIdByPaymentId(#id))")
    public ResponseEntity<ResponseWrapper<PaymentDTO>> getPaymentById(@PathVariable Integer id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Payment retrieved successfully", payment));
    }


    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#orderId)")
    public ResponseEntity<ResponseWrapper<PaymentDTO>> getPaymentByOrderId(@PathVariable Integer orderId) {
        PaymentDTO payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ResponseWrapper.success("Payment retrieved successfully", payment));
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PaymentDTO>> updatePaymentStatus(
            @PathVariable Integer id,
            @RequestParam String status) {

        Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status);
        PaymentDTO updatedPayment = paymentService.updatePaymentStatus(id, paymentStatus);
        return ResponseEntity.ok(ResponseWrapper.success("Payment status updated successfully", updatedPayment));
    }


    @PostMapping("/create-momo-url")
    public ResponseEntity<ResponseWrapper<String>> createMomoUrl(
            @RequestParam Integer orderId,
            HttpServletRequest request) {

        String paymentUrl = paymentService.createMomoPaymentUrl(orderId, request);
        return ResponseEntity.ok(ResponseWrapper.success("MoMo payment URL created successfully", paymentUrl));
    }


    @GetMapping("/momo-return")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> momoReturn(
            @RequestParam Map<String, String> queryParams,
            HttpServletRequest request) {

        Map<String, String> result = paymentService.processMomoReturn(queryParams, request);
        boolean isSuccess = Boolean.parseBoolean(result.get("success"));

        if (isSuccess) {
            return ResponseEntity.ok(ResponseWrapper.success("Payment processed successfully", result));
        } else {
            ResponseWrapper<Map<String, String>> response = new ResponseWrapper<>(false, result.get("message"), result);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @GetMapping("/momo-notify")
    public ResponseEntity<String> momoNotify(@RequestParam Map<String, String> queryParams) {
        boolean verified = paymentService.verifyMomoIpn(queryParams);

        if (verified) {
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("FAIL");
        }
    }
}