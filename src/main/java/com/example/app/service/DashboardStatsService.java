package com.example.app.service;

import com.example.app.dto.DashboardStatsDTO;

import java.time.LocalDateTime;
import java.util.Map;

public interface DashboardStatsService {
    /**
     * Lấy thống kê tổng quan (mặc định là tháng hiện tại)
     */
    DashboardStatsDTO getDashboardStats();

    /**
     * Lấy thống kê theo khoảng thời gian
     */
    DashboardStatsDTO getDashboardStatsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Lấy thống kê doanh số theo thời gian (ngày, tuần, tháng, năm)
     */
    Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate, String period);

    /**
     * Lấy thống kê doanh số theo thời gian (mặc định)
     */
    Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Lấy top sản phẩm bán chạy
     */
    Map<String, Object> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate, int limit);

    /**
     * Lấy thống kê doanh thu theo danh mục
     */
    Map<String, Object> getSalesByCategory(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Lấy thống kê người dùng mới
     */
    Map<String, Object> getNewUserStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Lấy danh sách sản phẩm sắp hết hàng
     */
    Map<String, Object> getLowStockProducts(int threshold);
}