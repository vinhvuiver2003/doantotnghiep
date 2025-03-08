package com.example.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Delivery")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Delivery_ID")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "Order_ID", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "Shipping_Status", nullable = false)
    private ShippingStatus shippingStatus = ShippingStatus.pending;

    @Column(name = "Shipping_Method", nullable = false)
    private String shippingMethod;

    @Column(name = "Tracking_Number")
    private String trackingNumber;

    @Column(name = "Shipping_Address", nullable = false)
    private String shippingAddress;

    @Column(name = "Contact_Phone", nullable = false)
    private String contactPhone;

    @Column(name = "Shipped_Date")
    private LocalDateTime shippedDate;

    @Column(name = "Delivered_Date")
    private LocalDateTime deliveredDate;

    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at")
    private LocalDateTime updatedAt;

    public enum ShippingStatus {
        pending, processing, shipped, delivered, failed
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}