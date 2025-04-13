package com.example.app.service;

import com.example.app.dto.DashboardStatsDTO;

import java.time.LocalDateTime;
import java.util.Map;

public interface DashboardStatsService {

    DashboardStatsDTO getDashboardStats();


    DashboardStatsDTO getDashboardStatsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate, String period);


    Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate);


    Map<String, Object> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, int limit);


    Map<String, Object> getSalesByCategory(LocalDateTime startDate, LocalDateTime endDate);


    Map<String, Object> getNewUserStatistics(LocalDateTime startDate, LocalDateTime endDate);

    Map<String, Object> getLowStockProducts(int threshold);
}