package com.example.app.service;
import com.example.app.dto.DashboardStatsDTO;

import java.time.LocalDateTime;

public interface DashboardStatsService {
    DashboardStatsDTO getDashboardStats();

    DashboardStatsDTO getDashboardStatsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
