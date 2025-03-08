package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDTO {
    private Integer id;
    private Integer orderId;
    private String shippingStatus;
    private String shippingMethod;
    private String trackingNumber;
    private String shippingAddress;
    private String contactPhone;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}