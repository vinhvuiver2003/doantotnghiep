package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Integer id;
    private Integer orderId;
    private String status;
    private BigDecimal amount;
    private String bankTransferCode;
    private String bankAccount;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
}
