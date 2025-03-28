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

        // Tổng số sản phẩm
        stats.setTotalProducts(productRepository.count());

        // Tổng số người dùng
        stats.setTotalUsers(userRepository.count());

        // Tổng số đơn hàng
        List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);
        stats.setTotalOrders(orders.size());

        // Tổng doanh thu
        BigDecimal totalSales = orders.stream()
                .filter(order -> order.getOrderStatus() != Order.OrderStatus.cancelled &&
                        order.getOrderStatus() != Order.OrderStatus.refunded)
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalSales(totalSales);

        // Đơn hàng đang chờ xử lý
        long pendingOrders = orders.stream()
                .filter(order -> order.getOrderStatus() == Order.OrderStatus.pending)
                .count();
        stats.setPendingOrders(pendingOrders);

        // Sản phẩm sắp hết hàng - sử dụng service thay vì truy vấn trực tiếp
        List<ProductDTO> lowStockProducts = productService.getLowStockProducts(10);
        stats.setLowStockProducts((long) lowStockProducts.size());

        // Phân phối trạng thái đơn hàng
        Map<String, Long> orderStatusCounts = new HashMap<>();
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            long count = orders.stream()
                    .filter(order -> order.getOrderStatus() == status)
                    .count();
            orderStatusCounts.put(status.name(), count);
        }
        stats.setOrderStatusDistribution(orderStatusCounts);

        // Doanh thu theo danh mục
        Map<String, BigDecimal> salesByCategory = calculateSalesByCategory(orders);
        stats.setSalesByCategory(salesByCategory);

        // Top sản phẩm bán chạy
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

        // Sắp xếp và lấy top 5 sản phẩm
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

        // Lọc bỏ đơn hàng đã hủy và hoàn tiền
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
                // Tính theo tuần (ngày đầu tiên của tuần)
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
                // Mặc định là monthly
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

        // Tính số lượng đơn hàng
        int orderCount = validOrders.size();

        // Tính giá trị trung bình đơn hàng
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

        // Lọc đơn hàng hợp lệ
        List<Order> validOrders = orders.stream()
                .filter(order -> order.getOrderStatus() != Order.OrderStatus.cancelled &&
                        order.getOrderStatus() != Order.OrderStatus.refunded)
                .collect(Collectors.toList());

        // Tính số lượng bán của từng sản phẩm
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

        // Sắp xếp theo số lượng bán và lấy top N sản phẩm
        List<Integer> topProductIds = productQuantities.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
        // Lấy chi tiết sản phẩm với một lần truy vấn
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

        // Lọc đơn hàng hợp lệ
        List<Order> validOrders = orders.stream()
                .filter(order -> order.getOrderStatus() != Order.OrderStatus.cancelled &&
                        order.getOrderStatus() != Order.OrderStatus.refunded)
                .collect(Collectors.toList());

        // Tính doanh thu theo danh mục
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

        // Tính tổng doanh thu
        BigDecimal totalRevenue = categoryRevenues.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tính phần trăm doanh thu của từng danh mục
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

        // Lấy danh sách người dùng mới
        List<User> newUsers = userRepository.findNewUsers(startDate);

        // Đếm số lượng người dùng mới theo ngày
        Map<String, Long> usersByDate = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (User user : newUsers) {
            String date = user.getCreatedAt().format(formatter);
            usersByDate.merge(date, 1L, Long::sum);
        }

        result.put("totalNewUsers", newUsers.size());
        result.put("usersByDate", usersByDate);

        // Tính tỷ lệ chuyển đổi (người dùng có đơn hàng / tổng số người dùng mới)
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

        // Sử dụng phương thức tối ưu để lấy sản phẩm sắp hết hàng
        List<Product> lowStockProductsWithDetails = productRepository.findLowStockProductsWithImages(threshold);
        
        // Tạo danh sách lowStockProducts từ entity
        List<ProductDTO> lowStockProducts = new ArrayList<>();
        for (Product product : lowStockProductsWithDetails) {
            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
            
            // Lấy mainImageUrl từ phương thức getMainImageUrl
            String mainImageUrl = product.getMainImageUrl();
            List<String> images = new ArrayList<>();
            images.add(mainImageUrl);
            dto.setImages(images);
            
            dto.setTotalStockQuantity(product.getTotalStockQuantity());
            dto.setBasePrice(product.getBasePrice());
            dto.setStatus(product.getStatus().toString());
            // Thêm các field cần thiết khác
            lowStockProducts.add(dto);
        }

        result.put("threshold", threshold);
        result.put("lowStockProducts", lowStockProducts);
        result.put("totalLowStock", lowStockProducts.size());

        // Phân loại theo danh mục
        Map<String, Long> lowStockByCategory = lowStockProducts.stream()
                .collect(Collectors.groupingBy(
                        ProductDTO::getCategoryName,
                        Collectors.counting()
                ));

        result.put("lowStockByCategory", lowStockByCategory);

        return result;
    }

    private Map<String, Object> getTopSellingCategories(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        // Implementation depends on your business requirements
        return null;
    }
}