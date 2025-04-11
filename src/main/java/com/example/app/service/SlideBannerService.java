package com.example.app.service;

import com.example.app.dto.SlideBannerDTO;
import com.example.app.entity.SlideBanner;
import com.example.app.repository.SlideBannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlideBannerService {

    @Autowired
    private SlideBannerRepository slideBannerRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private static final String BANNER_UPLOAD_DIR = "banners";

    public List<SlideBannerDTO> getAllActiveBanners() {
        return slideBannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SlideBannerDTO createBanner(SlideBannerDTO bannerDTO, MultipartFile imageFile) throws IOException {
        String imageUrl = fileStorageService.storeFile(imageFile, BANNER_UPLOAD_DIR, imageFile.getOriginalFilename());
        
        SlideBanner banner = new SlideBanner();
        banner.setTitle(bannerDTO.getTitle());
        banner.setImageUrl(imageUrl);
        banner.setLinkToCategory(bannerDTO.getLinkToCategory());
        banner.setDisplayOrder(bannerDTO.getDisplayOrder());
        banner.setIsActive(bannerDTO.getIsActive());

        SlideBanner savedBanner = slideBannerRepository.save(banner);
        return convertToDTO(savedBanner);
    }

    public SlideBannerDTO updateBanner(Long id, SlideBannerDTO bannerDTO, MultipartFile imageFile) throws IOException {
        SlideBanner banner = slideBannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        if (imageFile != null && !imageFile.isEmpty()) {
            String newImageUrl = fileStorageService.storeFile(imageFile, BANNER_UPLOAD_DIR, imageFile.getOriginalFilename());
            banner.setImageUrl(newImageUrl);
        }

        banner.setTitle(bannerDTO.getTitle());
        banner.setLinkToCategory(bannerDTO.getLinkToCategory());
        banner.setDisplayOrder(bannerDTO.getDisplayOrder());
        banner.setIsActive(bannerDTO.getIsActive());

        SlideBanner updatedBanner = slideBannerRepository.save(banner);
        return convertToDTO(updatedBanner);
    }

    public void deleteBanner(Long id) {
        slideBannerRepository.deleteById(id);
    }

    private SlideBannerDTO convertToDTO(SlideBanner banner) {
        SlideBannerDTO dto = new SlideBannerDTO();
        dto.setId(banner.getId());
        dto.setTitle(banner.getTitle());
        dto.setImageUrl(banner.getImageUrl());
        dto.setLinkToCategory(banner.getLinkToCategory());
        dto.setDisplayOrder(banner.getDisplayOrder());
        dto.setIsActive(banner.getIsActive());
        return dto;
    }
} 