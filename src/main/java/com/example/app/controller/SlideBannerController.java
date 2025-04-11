package com.example.app.controller;

import com.example.app.dto.SlideBannerDTO;
import com.example.app.service.SlideBannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class SlideBannerController {

    @Autowired
    private SlideBannerService slideBannerService;

    @GetMapping
    public ResponseEntity<List<SlideBannerDTO>> getAllActiveBanners() {
        return ResponseEntity.ok(slideBannerService.getAllActiveBanners());
    }

    @PostMapping
    public ResponseEntity<SlideBannerDTO> createBanner(
            @RequestPart("banner") SlideBannerDTO bannerDTO,
            @RequestPart("image") MultipartFile imageFile) throws IOException {
        return ResponseEntity.ok(slideBannerService.createBanner(bannerDTO, imageFile));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SlideBannerDTO> updateBanner(
            @PathVariable Long id,
            @RequestPart("banner") SlideBannerDTO bannerDTO,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        return ResponseEntity.ok(slideBannerService.updateBanner(id, bannerDTO, imageFile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        slideBannerService.deleteBanner(id);
        return ResponseEntity.ok().build();
    }
} 