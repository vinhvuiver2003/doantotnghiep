package com.example.app.controller;


import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.CheckoutRequest;
import com.example.app.dto.OrderDTO;
import com.example.app.dto.PagedResponse;
import com.example.app.entity.Order;
import com.example.app.entity.User;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Autowired
    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    /**
     * Lấy danh sách tất cả đơn hàng (chỉ ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PagedResponse<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<OrderDTO> orders = orderService.getAllOrders(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ResponseWrapper.success("Orders retrieved successfully", orders));
    }

    /**
     * Lấy thông tin chi tiết một đơn hàng theo ID (ADMIN hoặc chủ đơn hàng)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#id)")
    public ResponseEntity<ResponseWrapper<OrderDTO>> getOrderById(@PathVariable Integer id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Order retrieved successfully", order));
    }

    /**
     * Lấy danh sách đơn hàng của người dùng hiện tại
     */
    @GetMapping("/my-orders")
    public ResponseEntity<ResponseWrapper<PagedResponse<OrderDTO>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Lấy ID người dùng từ username (thực hiện trong OrderService)
        PagedResponse<OrderDTO> orders = orderService.getOrdersByCurrentUser(username, page, size);
        return ResponseEntity.ok(ResponseWrapper.success("Your orders retrieved successfully", orders));
    }

    /**
     * Lấy danh sách đơn hàng của một người dùng (chỉ ADMIN)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#userId)")
    public ResponseEntity<ResponseWrapper<PagedResponse<OrderDTO>>> getOrdersByUser(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<OrderDTO> orders = orderService.getOrdersByUser(userId, page, size);
        return ResponseEntity.ok(ResponseWrapper.success("User orders retrieved successfully", orders));
    }

    /**
     * Lấy danh sách đơn hàng theo trạng thái (chỉ ADMIN)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<List<OrderDTO>>> getOrdersByStatus(
            @PathVariable String status) {

        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status);
        List<OrderDTO> orders = orderService.getOrdersByStatus(orderStatus);
        return ResponseEntity.ok(ResponseWrapper.success("Orders by status retrieved successfully", orders));
    }

    /**
     * Tạo đơn hàng mới từ giỏ hàng (checkout)
     */
    @PostMapping("/checkout")
    public ResponseEntity<ResponseWrapper<OrderDTO>> checkout(
            @Valid @RequestBody CheckoutRequest checkoutRequest) {

        // Kiểm tra nếu người dùng đã đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getPrincipal().equals("anonymousUser")) {
            
            // Lấy thông tin người dùng hiện tại
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
            
            // Điền thông tin người dùng vào request nếu chưa có
            if (checkoutRequest.getUserId() == null) {
                checkoutRequest.setUserId(user.getId());
            }
            
            // Điền thông tin cá nhân từ tài khoản nếu chưa được cung cấp
            if (checkoutRequest.getEmail() == null || checkoutRequest.getEmail().isEmpty()) {
                checkoutRequest.setEmail(user.getEmail());
            }
            
            if (checkoutRequest.getName() == null || checkoutRequest.getName().isEmpty()) {
                checkoutRequest.setName(user.getFirstName() + " " + user.getLastName());
            }
            
            if (checkoutRequest.getPhone() == null || checkoutRequest.getPhone().isEmpty()) {
                checkoutRequest.setPhone(user.getPhone());
            }
        }

        OrderDTO newOrder = orderService.processCheckout(checkoutRequest);
        return new ResponseEntity<>(
                ResponseWrapper.success("Order created successfully", newOrder),
                HttpStatus.CREATED);
    }

    /**
     * Tạo đơn hàng mới (chỉ ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<OrderDTO>> createOrder(
            @Valid @RequestBody OrderDTO orderDTO) {

        OrderDTO newOrder = orderService.createOrder(orderDTO);
        return new ResponseEntity<>(
                ResponseWrapper.success("Order created successfully", newOrder),
                HttpStatus.CREATED);
    }

    /**
     * Cập nhật trạng thái đơn hàng (chỉ ADMIN)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<OrderDTO>> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam String status) {

        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status);
        OrderDTO updatedOrder = orderService.updateOrderStatus(id, orderStatus);
        return ResponseEntity.ok(ResponseWrapper.success("Order status updated successfully", updatedOrder));
    }

    /**
     * Xóa đơn hàng (chỉ ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<?>> deleteOrder(@PathVariable Integer id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ResponseWrapper.success("Order deleted successfully"));
    }

    /**
     * Lấy thống kê doanh số (chỉ ADMIN)
     */
    @GetMapping("/stats/sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getSalesStats(
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
        return ResponseEntity.ok(ResponseWrapper.success("Sales statistics retrieved successfully", salesStats));
    }

    /**
     * Người dùng xác nhận đã nhận hàng
     */
    @PatchMapping("/{id}/confirm-delivery")
    @PreAuthorize("@orderSecurity.isOwner(#id)")
    public ResponseEntity<ResponseWrapper<OrderDTO>> confirmDelivery(@PathVariable Integer id) {
        // Lấy thông tin người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        OrderDTO updatedOrder = orderService.confirmOrderDelivery(id, username);
        return ResponseEntity.ok(ResponseWrapper.success("Xác nhận đã nhận hàng thành công", updatedOrder));
    }

    /**
     * Người dùng hủy đơn hàng (chỉ có thể hủy đơn hàng ở trạng thái "pending")
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("@orderSecurity.isOwner(#id)")
    public ResponseEntity<ResponseWrapper<OrderDTO>> cancelOrderByUser(
            @PathVariable Integer id,
            @RequestParam(required = false) String cancelReason) {
        
        // Lấy thông tin người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        OrderDTO cancelledOrder = orderService.cancelOrderByUser(id, username, cancelReason);
        return ResponseEntity.ok(ResponseWrapper.success("Đơn hàng đã được hủy thành công", cancelledOrder));
    }
}