package com.example.app.service.impl;

import com.example.app.dto.DashboardStatsDTO;
import com.example.app.dto.ProductDTO;
import com.example.app.entity.Order;
import com.example.app.entity.OrderItem;
import com.example.app.entity.Product;
import com.example.app.entity.ProductImage;
import com.example.app.entity.ProductVariant;
import com.example.app.entity.User;
import com.example.app.repository.*;
import com.example.app.service.DashboardStatsService;
import com.example.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardStatsServiceImpl implements DashboardStatsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    @Autowired
    public DashboardStatsServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ProductService productService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    @Override
    public DashboardStatsDTO getDashboardStats() {
        // Lấy thống kê cho tháng hiện tại
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();

        return getDashboardStatsByDateRange(startOfMonth, now);
    }

    @Override
    public DashboardStatsDTO getDashboardStatsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        stats.setTotalProducts(productRepository.count());

        stats.setTotalUsers(userRepository.count());

        List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);
        stats.setTotalOrders(orders.size());

        BigDecimal totalSales = orders.stream()
                .filter(order -> order.getOrderStatus() != Order.OrderStatus.cancelled &&
                        order.getOrderStatus() != Order.OrderStatus.refunded)
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalSales(totalSales);

        long pendingOrders = orders.stream()
                .filter(order -> order.getOrderStatus() == Order.OrderStatus.pending)
                .count();
        stats.setPendingOrders(pendingOrders);

        List<ProductDTO> lowStockProducts = productService.getLowStockProducts(10);
        stats.setLowStockProducts((long) lowStockProducts.size());

        Map<String, Long> orderStatusCounts = new HashMap<>();
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            long count = orders.stream()
                    .filter(order -> order.getOrderStatus() == status)
                    .count();
            orderStatusCounts.put(status.name(), count);
        }
        stats.setOrderStatusDistribution(orderStatusCounts);

        Map<String, BigDecimal> salesByCategory = calculateSalesByCategory(orders);
        stats.setSalesByCategory(salesByCategory);

        Map<String, Long> topProducts = calculateTopSellingProducts(orders);
        stats.setTopSellingProducts(topProducts);

        return stats;
    }

    private Map<String, BigDecimal> calculateSalesByCategory(List<Order> orders) {
        Map<String, BigDecimal> salesByCategory = new HashMap<>();

        for (Order order : orders) {
            if (order.getOrderStatus() == Order.OrderStatus.cancelled ||
                    order.getOrderStatus() == Order.OrderStatus.refunded) {
                continue;
            }

            for (OrderItem item : order.getItems()) {
                String categoryName = item.getProduct().getCategory().getName();
                BigDecimal itemTotal = item.getTotal();

                salesByCategory.merge(categoryName, itemTotal, BigDecimal::add);
            }
        }

        return salesByCategory;
    }

    private Map<String, Long> calculateTopSellingProducts(List<Order> orders) {
        Map<Integer, Long> productQuantities = new HashMap<>();

        for (Order order : orders) {
            if (order.getOrderStatus() == Order.OrderStatus.cancelled ||
                    order.getOrderStatus() == Order.OrderStatus.refunded) {
                continue;
            }

            for (OrderItem item : order.getItems()) {
                int productId = item.getProduct().getId();
                long quantity = item.getQuantity();

                productQuantities.merge(productId, (long) quantity, Long::sum);
            }
        }

        return productQuantities.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        entry -> {
                            Product product = productRepository.findById(entry.getKey()).orElse(null);
                            return product != null ? product.getName() : "Unknown Product";
                        },
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    @Override
    public Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate, String period) {
        Map<String, Object> result = new HashMap<>();
        List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);

        List<Order> validOrders = orders.stream()
                .filter(order -> order.getOrderStatus() != Order.OrderStatus.cancelled &&
                        order.getOrderStatus() != Order.OrderStatus.refunded)
                .collect(Collectors.toList());

        DateTimeFormatter formatter;
        Map<String, BigDecimal> salesByPeriod = new LinkedHashMap<>();

        switch (period.toLowerCase()) {
            case "daily":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                for (Order order : validOrders) {
                    String date = order.getCreatedAt().format(formatter);
                    salesByPeriod.merge(date, order.getFinalAmount(), BigDecimal::add);
                }
                break;

            case "weekly":
                validOrders.forEach(order -> {
                    LocalDate date = order.getCreatedAt().toLocalDate();
                    LocalDate firstDayOfWeek = date.minusDays(date.getDayOfWeek().getValue() - 1);
                    String weekKey = firstDayOfWeek.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    salesByPeriod.merge(weekKey, order.getFinalAmount(), BigDecimal::add);
                });
                break;

            case "monthly":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                for (Order order : validOrders) {
                    String month = order.getCreatedAt().format(formatter);
                    salesByPeriod.merge(month, order.getFinalAmount(), BigDecimal::add);
                }
                break;

            case "yearly":
                formatter = DateTimeFormatter.ofPattern("yyyy");
                for (Order order : validOrders) {
                    String year = order.getCreatedAt().format(formatter);
                    salesByPeriod.merge(year, order.getFinalAmount(), BigDecimal::add);
                }
                break;

            default:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                for (Order order : validOrders) {
                    String month = order.getCreatedAt().format(formatter);
                    salesByPeriod.merge(month, order.getFinalAmount(), BigDecimal::add);
                }
        }

        // Tính tổng doanh thu
        BigDecimal totalSales = validOrders.stream()
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int orderCount = validOrders.size();

        BigDecimal avgOrderValue = orderCount > 0
                ? totalSales.divide(BigDecimal.valueOf(orderCount), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        result.put("totalSales", totalSales);
        result.put("orderCount", orderCount);
        result.put("avgOrderValue", avgOrderValue);
        result.put("salesByPeriod", salesByPeriod);
        result.put("period", period);

        return result;
    }

    @Override
    public Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return getSalesStatistics(startDate, endDate, "monthly");
    }

    @Override
    public Map<String, Object> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        Map<String, Object> result = new HashMap<>();

        List<Order> orders = orderRepository.findOrdersByDateRangeWithItems(startDate, endDate);

        List<Order> validOrders = orders.stream()
                .filter(order -> order.getOrderStatus() != Order.OrderStatus.cancelled &&
                        order.getOrderStatus() != Order.OrderStatus.refunded)
                .collect(Collectors.toList());

        Map<Integer, Long> productQuantities = new HashMap<>();
        Map<Integer, BigDecimal> productRevenues = new HashMap<>();

        for (Order order : validOrders) {
            for (OrderItem item : order.getItems()) {
                Integer productId = item.getProduct().getId();
                Long quantity = (long) item.getQuantity();
                BigDecimal revenue = item.getTotal();

                productQuantities.merge(productId, quantity, Long::sum);
                productRevenues.merge(productId, revenue, BigDecimal::add);
            }
        }

        List<Integer> topProductIds = productQuantities.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
        List<Product> topProductDetails = productRepository.findByIdInWithVariantsAndImages(topProductIds);
        Map<Integer, Product> productMap = topProductDetails.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Map<String, Object>> topProducts = topProductIds.stream()
                .map(productId -> {
                    Map<String, Object> productInfo = new HashMap<>();
                    Product product = productMap.get(productId);

                    if (product != null) {
                        productInfo.put("id", product.getId());
                        productInfo.put("name", product.getName());
                        productInfo.put("quantity", productQuantities.get(productId));
                        productInfo.put("revenue", productRevenues.getOrDefault(productId, BigDecimal.ZERO));
                        productInfo.put("category", product.getCategory().getName());
                        productInfo.put("image", product.getMainImageUrl());
                    }

                    return productInfo;
                })
                .collect(Collectors.toList());

        result.put("topProducts", topProducts);
        result.put("totalProducts", topProducts.size());

        return result;
    }

    @Override
    public Map<String, Object> getSalesByCategory(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> result = new HashMap<>();

        List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);

        List<Order> validOrders = orders.stream()
                .filter(order -> order.getOrderStatus() != Order.OrderStatus.cancelled &&
                        order.getOrderStatus() != Order.OrderStatus.refunded)
                .collect(Collectors.toList());

        Map<String, BigDecimal> categoryRevenues = new HashMap<>();
        Map<String, Long> categoryItemCounts = new HashMap<>();

        for (Order order : validOrders) {
            for (OrderItem item : order.getItems()) {
                String categoryName = item.getProduct().getCategory().getName();
                BigDecimal revenue = item.getTotal();
                Long quantity = (long) item.getQuantity();

                categoryRevenues.merge(categoryName, revenue, BigDecimal::add);
                categoryItemCounts.merge(categoryName, quantity, Long::sum);
            }
        }

        BigDecimal totalRevenue = categoryRevenues.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> categoriesData = categoryRevenues.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Map<String, Object> categoryData = new HashMap<>();
                            categoryData.put("revenue", entry.getValue());
                            categoryData.put("percentage", totalRevenue.compareTo(BigDecimal.ZERO) > 0
                                    ? entry.getValue().multiply(new BigDecimal("100")).divide(totalRevenue, 2, BigDecimal.ROUND_HALF_UP)
                                    : BigDecimal.ZERO);
                            categoryData.put("itemsSold", categoryItemCounts.getOrDefault(entry.getKey(), 0L));
                            return categoryData;
                        },
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        result.put("categories", categoriesData);
        result.put("totalRevenue", totalRevenue);

        return result;
    }

    @Override
    public Map<String, Object> getNewUserStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> result = new HashMap<>();

        List<User> newUsers = userRepository.findNewUsers(startDate);

        Map<String, Long> usersByDate = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (User user : newUsers) {
            String date = user.getCreatedAt().format(formatter);
            usersByDate.merge(date, 1L, Long::sum);
        }

        result.put("totalNewUsers", newUsers.size());
        result.put("usersByDate", usersByDate);

        long usersWithOrders = 0;
        for (User user : newUsers) {
            long orderCount = orderRepository.countCompletedOrdersByUser(user.getId());
            if (orderCount > 0) {
                usersWithOrders++;
            }
        }

        double conversionRate = newUsers.size() > 0
                ? (double) usersWithOrders / newUsers.size() * 100
                : 0;

        result.put("usersWithOrders", usersWithOrders);
        result.put("conversionRate", conversionRate);

        return result;
    }

    @Override
    public Map<String, Object> getLowStockProducts(int threshold) {
        Map<String, Object> result = new HashMap<>();

        List<Product> lowStockProductsWithDetails = productRepository.findLowStockProductsWithImages(threshold);
        
        List<ProductDTO> lowStockProducts = new ArrayList<>();
        for (Product product : lowStockProductsWithDetails) {
            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
            
            String mainImageUrl = product.getMainImageUrl();
            List<String> images = new ArrayList<>();
            images.add(mainImageUrl);
            dto.setImages(images);
            
            dto.setTotalStockQuantity(product.getTotalStockQuantity());
            dto.setBasePrice(product.getBasePrice());
            dto.setStatus(product.getStatus().toString());
            lowStockProducts.add(dto);
        }

        result.put("threshold", threshold);
        result.put("lowStockProducts", lowStockProducts);
        result.put("totalLowStock", lowStockProducts.size());

        Map<String, Long> lowStockByCategory = lowStockProducts.stream()
                .collect(Collectors.groupingBy(
                        ProductDTO::getCategoryName,
                        Collectors.counting()
                ));

        result.put("lowStockByCategory", lowStockByCategory);

        return result;
    }

    private Map<String, Object> getTopSellingCategories(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        return null;
    }
}