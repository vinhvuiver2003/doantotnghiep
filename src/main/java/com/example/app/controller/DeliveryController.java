package com.example.app.controller;

import com.example.app.dto.ResponseWrapper;
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


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#deliveryService.getOrderIdByDeliveryId(#id))")
    public ResponseEntity<ResponseWrapper<DeliveryDTO>> getDeliveryById(@PathVariable Integer id) {
        DeliveryDTO delivery = deliveryService.getDeliveryById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Delivery retrieved successfully", delivery));
    }


    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#orderId)")
    public ResponseEntity<ResponseWrapper<DeliveryDTO>> getDeliveryByOrderId(@PathVariable Integer orderId) {
        DeliveryDTO delivery = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(ResponseWrapper.success("Delivery retrieved successfully", delivery));
    }


    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<List<DeliveryDTO>>> getDeliveriesByStatus(@PathVariable String status) {
        Delivery.ShippingStatus shippingStatus = Delivery.ShippingStatus.valueOf(status);
        List<DeliveryDTO> deliveries = deliveryService.getDeliveriesByStatus(shippingStatus);
        return ResponseEntity.ok(ResponseWrapper.success("Deliveries retrieved successfully", deliveries));
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<DeliveryDTO>> createDelivery(@Valid @RequestBody DeliveryDTO deliveryDTO) {
        DeliveryDTO createdDelivery = deliveryService.createDelivery(deliveryDTO);
        return new ResponseEntity<>(
                ResponseWrapper.success("Delivery created successfully", createdDelivery),
                HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<DeliveryDTO>> updateDelivery(
            @PathVariable Integer id,
            @Valid @RequestBody DeliveryDTO deliveryDTO) {

        DeliveryDTO updatedDelivery = deliveryService.updateDelivery(id, deliveryDTO);
        return ResponseEntity.ok(ResponseWrapper.success("Delivery updated successfully", updatedDelivery));
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<DeliveryDTO>> updateDeliveryStatus(
            @PathVariable Integer id,
            @RequestParam String status) {

        Delivery.ShippingStatus shippingStatus = Delivery.ShippingStatus.valueOf(status);
        DeliveryDTO updatedDelivery = deliveryService.updateDeliveryStatus(id, shippingStatus);
        return ResponseEntity.ok(ResponseWrapper.success("Delivery status updated successfully", updatedDelivery));
    }


    @PatchMapping("/{id}/tracking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<DeliveryDTO>> updateTrackingNumber(
            @PathVariable Integer id,
            @RequestParam String trackingNumber) {

        DeliveryDTO updatedDelivery = deliveryService.updateTrackingNumber(id, trackingNumber);
        return ResponseEntity.ok(ResponseWrapper.success("Tracking number updated successfully", updatedDelivery));
    }


    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ResponseWrapper<List<DeliveryDTO>>> findByTrackingNumber(@PathVariable String trackingNumber) {
        List<DeliveryDTO> deliveries = deliveryService.findByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ResponseWrapper.success("Deliveries found", deliveries));
    }
}