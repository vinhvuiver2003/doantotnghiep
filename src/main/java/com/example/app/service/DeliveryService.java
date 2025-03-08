package com.example.app.service;

import com.example.app.dto.DeliveryDTO;
import com.example.app.entity.Delivery;

import java.util.List;

public interface DeliveryService {
    DeliveryDTO getDeliveryById(Integer id);

    DeliveryDTO getDeliveryByOrderId(Integer orderId);

    List<DeliveryDTO> getDeliveriesByStatus(Delivery.ShippingStatus status);

    DeliveryDTO createDelivery(DeliveryDTO deliveryDTO);

    DeliveryDTO updateDelivery(Integer id, DeliveryDTO deliveryDTO);

    DeliveryDTO updateDeliveryStatus(Integer id, Delivery.ShippingStatus status);

    DeliveryDTO updateTrackingNumber(Integer id, String trackingNumber);

    List<DeliveryDTO> findByTrackingNumber(String trackingNumber);

    Integer getOrderIdByDeliveryId(Integer deliveryId);
}