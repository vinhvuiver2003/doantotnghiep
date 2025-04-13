package com.example.app.service;
import com.example.app.dto.CheckoutRequest;
import com.example.app.dto.OrderDTO;
import com.example.app.dto.PagedResponse;
import com.example.app.entity.Order;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {

    PagedResponse<OrderDTO> getAllOrders(int page, int size, String sortBy, String sortDir);

    OrderDTO getOrderById(Integer id);

    PagedResponse<OrderDTO> getOrdersByUser(Integer userId, int page, int size);

    OrderDTO createOrder(OrderDTO orderDTO);

    OrderDTO processCheckout(CheckoutRequest checkoutRequest);

    OrderDTO updateOrderStatus(Integer id, Order.OrderStatus status);

    OrderDTO confirmOrderDelivery(Integer orderId, String username);

    void deleteOrder(Integer id);

    List<OrderDTO> getOrdersByStatus(Order.OrderStatus status);

    BigDecimal calculateTotalSales(LocalDateTime startDate, LocalDateTime endDate);

    Map<String, Long> getOrderStatusDistribution();

    Long countOrders();

    Long countPendingOrders();
    Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate);
    PagedResponse<OrderDTO> getOrdersByCurrentUser(String username, int page, int size);


    OrderDTO cancelOrderByUser(Integer orderId, String username, String cancelReason);
}