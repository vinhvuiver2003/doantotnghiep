package com.example.app.controller;

import com.example.app.dto.ResponseWrapper;
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


    @GetMapping("/stats")
    public ResponseEntity<ResponseWrapper<DashboardStatsDTO>> getDashboardStats() {
        DashboardStatsDTO stats = dashboardStatsService.getDashboardStats();
        return ResponseEntity.ok(ResponseWrapper.success("Dashboard statistics retrieved successfully", stats));
    }


    @GetMapping("/stats/date-range")
    public ResponseEntity<ResponseWrapper<DashboardStatsDTO>> getDashboardStatsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        DashboardStatsDTO stats = dashboardStatsService.getDashboardStatsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ResponseWrapper.success("Dashboard statistics retrieved successfully", stats));
    }


    @GetMapping("/sales/{period}")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> getSalesByPeriod(
            @PathVariable String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {


        if (startDate == null) {

            startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }

        if (endDate == null) {

            endDate = LocalDateTime.now();
        }

        Map<String, Object> salesStats = dashboardStatsService.getSalesStatistics(startDate, endDate);
        return ResponseEntity.ok(ResponseWrapper.success("Sales statistics retrieved successfully", salesStats));
    }
}