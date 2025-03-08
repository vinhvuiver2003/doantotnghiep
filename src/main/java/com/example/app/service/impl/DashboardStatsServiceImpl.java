package com.example.app.service.impl;

import com.example.app.dto.DashboardStatsDTO;
import com.example.app.entity.Order;
import com.example.app.repository.OrderItemRepository;
import com.example.app.repository.OrderRepository;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.UserRepository;
import com.example.app.service.DashboardStatsService;
import com.example.app.service.OrderService;
import com.example.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardStatsServiceImpl implements DashboardStatsService {

    private final OrderService orderService;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public DashboardStatsServiceImpl(
            OrderService orderService,
            ProductService productService,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.orderService = orderService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DashboardStatsDTO getDashboardStats() {
        // Get current time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        return getDashboardStatsByDateRange(startOfMonth, now);
    }

    @Override
    public DashboardStatsDTO getDashboardStatsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Count total products
        stats.setTotalProducts(productService.countProducts());

        // Count total users
        stats.setTotalUsers(userRepository.count());

        // Count total orders
        stats.setTotalOrders(orderService.countOrders());

        // Calculate total sales
        stats.setTotalSales(orderService.calculateTotalSales(startDate, endDate));

        // Count pending orders
        stats.setPendingOrders(orderService.countPendingOrders());

        // Count low stock products
        stats.setLowStockProducts((long) productService.getLowStockProducts(10).size());

        // Get order status distribution
        stats.setOrderStatusDistribution(orderService.getOrderStatusDistribution());

        // Get sales by category
        Map<String, BigDecimal> salesByCategory = new HashMap<>();
        List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);

        orders.stream()
                .filter(order -> order.getOrderStatus() != Order.OrderStatus.cancelled &&
                        order.getOrderStatus() != Order.OrderStatus.refunded)
                .flatMap(order -> order.getItems().stream())
                .forEach(item -> {
                    String categoryName = item.getProduct().getCategory().getName();
                    BigDecimal itemTotal = item.getTotal();

                    salesByCategory.merge(categoryName, itemTotal, BigDecimal::add);
                });

        stats.setSalesByCategory(salesByCategory);

        // Get top selling products
        List<Object[]> topSellingProducts = orderItemRepository.findBestSellingProducts(PageRequest.of(0, 5));
        Map<String, Long> topProducts = new HashMap<>();

        for (Object[] result : topSellingProducts) {
            Integer productId = (Integer) result[0];
            Long quantity = (Long) result[1];
            String productName = productRepository.findById(productId)
                    .map(p -> p.getName())
                    .orElse("Unknown Product");

            topProducts.put(productName, quantity);
        }

        stats.setTopSellingProducts(topProducts);

        return stats;
    }
    @Override
    public Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return orderService.getSalesStatistics(startDate, endDate);
    }
}



