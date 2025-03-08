package com.example.app.controller;

import com.example.app.dto.ApiResponse;
import com.example.app.dto.DashboardStatsDTO;
import com.example.app.service.DashboardStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    @Autowired
    public DashboardController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }

    /**
     * Lấy thống kê tổng quan (mặc định là tháng hiện tại)
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        DashboardStatsDTO stats = dashboardStatsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", stats));
    }

    /**
     * Lấy thống kê theo khoảng thời gian
     */
    @GetMapping("/stats/date-range")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStatsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        DashboardStatsDTO stats = dashboardStatsService.getDashboardStatsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", stats));
    }

    /**
     * Lấy thống kê bán hàng theo thời gian (ngày, tuần, tháng, năm)
     */
    @GetMapping("/sales/{period}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSalesByPeriod(
            @PathVariable String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Logic để lấy thống kê doanh số theo khoảng thời gian
        // Các giá trị cho period: daily, weekly, monthly, yearly

        if (startDate == null) {
            // Mặc định là đầu tháng hiện tại
            startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }

        if (endDate == null) {
            // Mặc định là thời điểm hiện tại
            endDate = LocalDateTime.now();
        }

        Map<String, Object> salesStats = dashboardStatsService.getSalesStatistics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Sales statistics retrieved successfully", salesStats));
    }
}