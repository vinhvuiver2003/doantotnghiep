package com.example.app.repository;

import com.example.app.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByOrderId(Integer orderId);

    List<Payment> findByStatus(Payment.PaymentStatus status);

    List<Payment> findByBankTransferCode(String bankTransferCode);
}