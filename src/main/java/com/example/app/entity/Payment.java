package com.example.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Payment_ID")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "Order_ID", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private PaymentStatus status = PaymentStatus.pending;

    @Column(name = "Amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "Bank_Transfer_Code")
    private String bankTransferCode;

    @Column(name = "Bank_Account")
    private String bankAccount;

    @Column(name = "Payment_Date")
    private LocalDateTime paymentDate;

    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum PaymentStatus {
        pending, completed, failed
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}