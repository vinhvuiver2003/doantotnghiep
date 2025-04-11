package com.example.app.repository;

import com.example.app.entity.SlideBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlideBannerRepository extends JpaRepository<SlideBanner, Long> {
    List<SlideBanner> findByIsActiveTrueOrderByDisplayOrderAsc();
} 