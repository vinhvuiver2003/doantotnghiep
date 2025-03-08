package com.example.app.controller;

import com.example.app.dto.ApiResponse;
import com.example.app.dto.DeliveryDTO;
import com.example.app.entity.Delivery;
import com.example.app.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Autowired
    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /**
     * Lấy thông tin giao hàng theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#deliveryService.getOrderIdByDeliveryId(#id))")
    public ResponseEntity<ApiResponse<DeliveryDTO>> getDeliveryById(@PathVariable Integer id) {
        DeliveryDTO delivery = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(ApiResponse.success("Delivery retrieved successfully", delivery));
    }

    /**
     * Lấy thông tin giao hàng theo đơn hàng
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#orderId)")
    public ResponseEntity<ApiResponse<DeliveryDTO>> getDeliveryByOrderId(@PathVariable Integer orderId) {
        DeliveryDTO delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Delivery retrieved successfully", delivery));
    }

    /**
     * Lấy danh sách giao hàng theo trạng thái (chỉ ADMIN)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getDeliveriesByStatus(@PathVariable String status) {
        Delivery.ShippingStatus shippingStatus = Delivery.ShippingStatus.valueOf(status);
        List<DeliveryDTO> deliveries = deliveryService.getDeliveriesByStatus(shippingStatus);
        return ResponseEntity.ok(ApiResponse.success("Deliveries retrieved successfully", deliveries));
    }

    /**
     * Tạo thông tin giao hàng mới (chỉ ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryDTO>> createDelivery(@Valid @RequestBody DeliveryDTO deliveryDTO) {
        DeliveryDTO createdDelivery = deliveryService.createDelivery(deliveryDTO);
        return new ResponseEntity<>(
                ApiResponse.success("Delivery created successfully", createdDelivery),
                HttpStatus.CREATED);
    }

    /**
     * Cập nhật thông tin giao hàng (chỉ ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryDTO>> updateDelivery(
            @PathVariable Integer id,
            @Valid @RequestBody DeliveryDTO deliveryDTO) {

        DeliveryDTO updatedDelivery = deliveryService.updateDelivery(id, deliveryDTO);
        return ResponseEntity.ok(ApiResponse.success("Delivery updated successfully", updatedDelivery));
    }

    /**
     * Cập nhật trạng thái giao hàng (chỉ ADMIN)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryDTO>> updateDeliveryStatus(
            @PathVariable Integer id,
            @RequestParam String status) {

        Delivery.ShippingStatus shippingStatus = Delivery.ShippingStatus.valueOf(status);
        DeliveryDTO updatedDelivery = deliveryService.updateDeliveryStatus(id, shippingStatus);
        return ResponseEntity.ok(ApiResponse.success("Delivery status updated successfully", updatedDelivery));
    }

    /**
     * Cập nhật mã vận đơn (tracking number) (chỉ ADMIN)
     */
    @PatchMapping("/{id}/tracking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryDTO>> updateTrackingNumber(
            @PathVariable Integer id,
            @RequestParam String trackingNumber) {

        DeliveryDTO updatedDelivery = deliveryService.updateTrackingNumber(id, trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Tracking number updated successfully", updatedDelivery));
    }

    /**
     * Tìm kiếm theo mã vận đơn (tracking number)
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ApiResponse<List<DeliveryDTO>>> findByTrackingNumber(@PathVariable String trackingNumber) {
        List<DeliveryDTO> deliveries = deliveryService.findByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Deliveries found", deliveries));
    }
}