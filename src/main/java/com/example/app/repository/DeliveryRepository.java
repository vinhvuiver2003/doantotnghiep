package com.example.app.repository;
import com.example.app.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    Optional<Delivery> findByOrderId(Integer orderId);

    List<Delivery> findByShippingStatus(Delivery.ShippingStatus status);

    List<Delivery> findByTrackingNumber(String trackingNumber);
}
