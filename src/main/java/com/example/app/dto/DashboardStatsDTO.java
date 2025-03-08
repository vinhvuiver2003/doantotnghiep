package com.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalProducts;
    private long totalUsers;
    private long totalOrders;
    private BigDecimal totalSales;
    private long pendingOrders;
    private long lowStockProducts;
    private Map<String, Long> orderStatusDistribution;
    private Map<String, BigDecimal> salesByCategory;
    private Map<String, Long> topSellingProducts;
}