package com.example.app.controller;


import com.example.app.dto.ApiResponse;
import com.example.app.dto.CheckoutRequest;
import com.example.app.dto.OrderDTO;
import com.example.app.dto.PagedResponse;
import com.example.app.entity.Order;
import com.example.app.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Lấy danh sách tất cả đơn hàng (chỉ ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<OrderDTO> orders = orderService.getAllOrders(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    /**
     * Lấy thông tin chi tiết một đơn hàng theo ID (ADMIN hoặc chủ đơn hàng)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#id)")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Integer id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    /**
     * Lấy danh sách đơn hàng của người dùng hiện tại
     */
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<PagedResponse<OrderDTO>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Lấy ID người dùng từ username (thực hiện trong OrderService)
        PagedResponse<OrderDTO> orders = orderService.getOrdersByCurrentUser(username, page, size);
        return ResponseEntity.ok(ApiResponse.success("Your orders retrieved successfully", orders));
    }

    /**
     * Lấy danh sách đơn hàng của một người dùng (chỉ ADMIN)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<ApiResponse<PagedResponse<OrderDTO>>> getOrdersByUser(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<OrderDTO> orders = orderService.getOrdersByUser(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("User orders retrieved successfully", orders));
    }

    /**
     * Lấy danh sách đơn hàng theo trạng thái (chỉ ADMIN)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getOrdersByStatus(
            @PathVariable String status) {

        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status);
        List<OrderDTO> orders = orderService.getOrdersByStatus(orderStatus);
        return ResponseEntity.ok(ApiResponse.success("Orders by status retrieved successfully", orders));
    }

    /**
     * Tạo đơn hàng mới từ giỏ hàng (checkout)
     */
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderDTO>> checkout(
            @Valid @RequestBody CheckoutRequest checkoutRequest) {

        OrderDTO newOrder = orderService.processCheckout(checkoutRequest);
        return new ResponseEntity<>(
                ApiResponse.success("Order created successfully", newOrder),
                HttpStatus.CREATED);
    }

    /**
     * Tạo đơn hàng mới (chỉ ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @Valid @RequestBody OrderDTO orderDTO) {

        OrderDTO newOrder = orderService.createOrder(orderDTO);
        return new ResponseEntity<>(
                ApiResponse.success("Order created successfully", newOrder),
                HttpStatus.CREATED);
    }

    /**
     * Cập nhật trạng thái đơn hàng (chỉ ADMIN)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam String status) {

        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status);
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, orderStatus);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updatedOrder));
    }

    /**
     * Xóa đơn hàng (chỉ ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteOrder(@PathVariable Integer id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order deleted successfully"));
    }

    /**
     * Lấy thống kê doanh số (chỉ ADMIN)
     */
    @GetMapping("/stats/sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalesStats(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        // Nếu không có startDate, lấy đầu tháng hiện tại
        if (startDate == null) {
            LocalDateTime now = LocalDateTime.now();
            startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }

        // Nếu không có endDate, lấy thời điểm hiện tại
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> salesStats = orderService.getSalesStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Sales statistics retrieved successfully", salesStats));
    }
}