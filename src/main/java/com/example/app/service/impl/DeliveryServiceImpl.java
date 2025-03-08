package com.example.app.service.impl;

import com.example.app.dto.DeliveryDTO;
import com.example.app.entity.Delivery;
import com.example.app.entity.Order;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.DeliveryRepository;
import com.example.app.repository.OrderRepository;
import com.example.app.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public DeliveryServiceImpl(
            DeliveryRepository deliveryRepository,
            OrderRepository orderRepository) {
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public DeliveryDTO getDeliveryById(Integer id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));

        return convertToDTO(delivery);
    }

    @Override
    public DeliveryDTO getDeliveryByOrderId(Integer orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found for order id: " + orderId));

        return convertToDTO(delivery);
    }

    @Override
    public List<DeliveryDTO> getDeliveriesByStatus(Delivery.ShippingStatus status) {
        List<Delivery> deliveries = deliveryRepository.findByShippingStatus(status);

        return deliveries.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeliveryDTO createDelivery(DeliveryDTO deliveryDTO) {
        // Kiểm tra đơn hàng tồn tại
        Order order = orderRepository.findById(deliveryDTO.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + deliveryDTO.getOrderId()));

        // Kiểm tra xem đã có thông tin giao hàng cho đơn hàng này chưa
        deliveryRepository.findByOrderId(deliveryDTO.getOrderId())
                .ifPresent(delivery -> {
                    throw new IllegalArgumentException("Delivery already exists for order id: " + deliveryDTO.getOrderId());
                });

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setShippingStatus(Delivery.ShippingStatus.valueOf(deliveryDTO.getShippingStatus()));
        delivery.setShippingMethod(deliveryDTO.getShippingMethod());
        delivery.setTrackingNumber(deliveryDTO.getTrackingNumber());
        delivery.setShippingAddress(deliveryDTO.getShippingAddress());
        delivery.setContactPhone(deliveryDTO.getContactPhone());

        // Nếu trạng thái là "shipped", đặt shipped date
        if (delivery.getShippingStatus() == Delivery.ShippingStatus.shipped) {
            delivery.setShippedDate(LocalDateTime.now());
        }

        // Nếu trạng thái là "delivered", đặt delivered date
        if (delivery.getShippingStatus() == Delivery.ShippingStatus.delivered) {
            delivery.setDeliveredDate(LocalDateTime.now());
        }

        Delivery savedDelivery = deliveryRepository.save(delivery);
        return convertToDTO(savedDelivery);
    }

    @Override
    @Transactional
    public DeliveryDTO updateDelivery(Integer id, DeliveryDTO deliveryDTO) {
        // Kiểm tra thông tin giao hàng tồn tại
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));

        // Cập nhật thông tin
        delivery.setShippingMethod(deliveryDTO.getShippingMethod());
        delivery.setTrackingNumber(deliveryDTO.getTrackingNumber());
        delivery.setShippingAddress(deliveryDTO.getShippingAddress());
        delivery.setContactPhone(deliveryDTO.getContactPhone());

        // Cập nhật trạng thái nếu thay đổi
        if (!delivery.getShippingStatus().name().equals(deliveryDTO.getShippingStatus())) {
            Delivery.ShippingStatus newStatus = Delivery.ShippingStatus.valueOf(deliveryDTO.getShippingStatus());
            delivery.setShippingStatus(newStatus);

            // Cập nhật các mốc thời gian liên quan đến trạng thái
            if (newStatus == Delivery.ShippingStatus.shipped && delivery.getShippedDate() == null) {
                delivery.setShippedDate(LocalDateTime.now());
            }

            if (newStatus == Delivery.ShippingStatus.delivered && delivery.getDeliveredDate() == null) {
                delivery.setDeliveredDate(LocalDateTime.now());
            }
        }

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToDTO(updatedDelivery);
    }

    @Override
    @Transactional
    public DeliveryDTO updateDeliveryStatus(Integer id, Delivery.ShippingStatus status) {
        // Kiểm tra thông tin giao hàng tồn tại
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));

        // Cập nhật trạng thái
        delivery.setShippingStatus(status);

        // Cập nhật các mốc thời gian liên quan đến trạng thái
        if (status == Delivery.ShippingStatus.shipped && delivery.getShippedDate() == null) {
            delivery.setShippedDate(LocalDateTime.now());
        }

        if (status == Delivery.ShippingStatus.delivered && delivery.getDeliveredDate() == null) {
            delivery.setDeliveredDate(LocalDateTime.now());
        }

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToDTO(updatedDelivery);
    }

    @Override
    @Transactional
    public DeliveryDTO updateTrackingNumber(Integer id, String trackingNumber) {
        // Kiểm tra thông tin giao hàng tồn tại
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));

        // Cập nhật mã vận đơn
        delivery.setTrackingNumber(trackingNumber);

        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return convertToDTO(updatedDelivery);
    }

    @Override
    public List<DeliveryDTO> findByTrackingNumber(String trackingNumber) {
        List<Delivery> deliveries = deliveryRepository.findByTrackingNumber(trackingNumber);

        return deliveries.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getOrderIdByDeliveryId(Integer deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        return delivery.getOrder().getId();
    }

    // Utility method to convert Entity to DTO
    private DeliveryDTO convertToDTO(Delivery delivery) {
        DeliveryDTO dto = new DeliveryDTO();
        dto.setId(delivery.getId());
        dto.setOrderId(delivery.getOrder().getId());
        dto.setShippingStatus(delivery.getShippingStatus().name());
        dto.setShippingMethod(delivery.getShippingMethod());
        dto.setTrackingNumber(delivery.getTrackingNumber());
        dto.setShippingAddress(delivery.getShippingAddress());
        dto.setContactPhone(delivery.getContactPhone());
        dto.setShippedDate(delivery.getShippedDate());
        dto.setDeliveredDate(delivery.getDeliveredDate());
        dto.setCreatedAt(delivery.getCreatedAt());
        dto.setUpdatedAt(delivery.getUpdatedAt());

        return dto;
    }
}