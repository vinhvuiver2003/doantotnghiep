package com.example.app.service.impl;

import com.example.app.dto.CategoryDTO;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.PromotionDTO;
import com.example.app.entity.Category;
import com.example.app.entity.Promotion;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.CategoryRepository;
import com.example.app.repository.PromotionRepository;
import com.example.app.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public PromotionServiceImpl(
            PromotionRepository promotionRepository,
            CategoryRepository categoryRepository) {
        this.promotionRepository = promotionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public PagedResponse<PromotionDTO> getAllPromotions(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Promotion> promotions = promotionRepository.findAll(pageable);

        List<PromotionDTO> content = promotions.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                promotions.getNumber(),
                promotions.getSize(),
                promotions.getTotalElements(),
                promotions.getTotalPages(),
                promotions.isLast()
        );
    }

    @Override
    public PromotionDTO getPromotionById(Integer id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

        return convertToDTO(promotion);
    }

    @Override
    public PromotionDTO getPromotionByCode(String code) {
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with code: " + code));

        return convertToDTO(promotion);
    }

    @Override
    @Transactional
    public PromotionDTO createPromotion(PromotionDTO promotionDTO) {
        if (promotionDTO.getStartDate().isAfter(promotionDTO.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        if (promotionDTO.getCode() != null && !promotionDTO.getCode().isEmpty()) {
            promotionRepository.findByCode(promotionDTO.getCode())
                    .ifPresent(p -> {
                        throw new IllegalArgumentException("Promotion code already exists: " + promotionDTO.getCode());
                    });
        }

        Promotion promotion = new Promotion();
        promotion.setName(promotionDTO.getName());
        promotion.setDescription(promotionDTO.getDescription());
        promotion.setDiscountType(Promotion.DiscountType.valueOf(promotionDTO.getDiscountType()));
        promotion.setDiscountValue(promotionDTO.getDiscountValue());
        promotion.setCode(promotionDTO.getCode());
        promotion.setMinimumOrder(promotionDTO.getMinimumOrder());
        promotion.setStartDate(promotionDTO.getStartDate());
        promotion.setEndDate(promotionDTO.getEndDate());
        promotion.setUsageLimit(promotionDTO.getUsageLimit());
        promotion.setUsageCount(0);

        if (promotionDTO.getStatus() != null) {
            promotion.setStatus(Promotion.PromotionStatus.valueOf(promotionDTO.getStatus()));
        }

        if (promotionDTO.getCategories() != null && !promotionDTO.getCategories().isEmpty()) {
            Set<Category> categories = new HashSet<>();

            for (CategoryDTO categoryDTO : promotionDTO.getCategories()) {
                Category category = categoryRepository.findById(categoryDTO.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryDTO.getId()));
                categories.add(category);
            }

            promotion.setCategories(categories);
        }

        Promotion savedPromotion = promotionRepository.save(promotion);

        return convertToDTO(savedPromotion);
    }

    @Override
    @Transactional
    public PromotionDTO updatePromotion(Integer id, PromotionDTO promotionDTO) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

        if (promotionDTO.getStartDate().isAfter(promotionDTO.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        if (promotionDTO.getCode() != null && !promotionDTO.getCode().isEmpty() &&
                !promotionDTO.getCode().equals(promotion.getCode())) {
            promotionRepository.findByCode(promotionDTO.getCode())
                    .ifPresent(p -> {
                        throw new IllegalArgumentException("Promotion code already exists: " + promotionDTO.getCode());
                    });
        }

        promotion.setName(promotionDTO.getName());
        promotion.setDescription(promotionDTO.getDescription());
        promotion.setDiscountType(Promotion.DiscountType.valueOf(promotionDTO.getDiscountType()));
        promotion.setDiscountValue(promotionDTO.getDiscountValue());
        promotion.setCode(promotionDTO.getCode());
        promotion.setMinimumOrder(promotionDTO.getMinimumOrder());
        promotion.setStartDate(promotionDTO.getStartDate());
        promotion.setEndDate(promotionDTO.getEndDate());
        promotion.setUsageLimit(promotionDTO.getUsageLimit());

        if (promotionDTO.getStatus() != null) {
            promotion.setStatus(Promotion.PromotionStatus.valueOf(promotionDTO.getStatus()));
        }

        if (promotionDTO.getCategories() != null) {
            Set<Category> categories = new HashSet<>();

            for (CategoryDTO categoryDTO : promotionDTO.getCategories()) {
                Category category = categoryRepository.findById(categoryDTO.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryDTO.getId()));
                categories.add(category);
            }

            promotion.setCategories(categories);
        }

        Promotion updatedPromotion = promotionRepository.save(promotion);

        return convertToDTO(updatedPromotion);
    }

    @Override
    @Transactional
    public void deletePromotion(Integer id) {
        if (!promotionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Promotion not found with id: " + id);
        }

        promotionRepository.deleteById(id);
    }

    @Override
    public List<PromotionDTO> getActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findActivePromotions(now);

        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionDTO> getPromotionsByCategory(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }

        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findPromotionsByCategory(categoryId, now);

        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PromotionDTO validatePromotion(String code) {
        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with code: " + code));

        LocalDateTime now = LocalDateTime.now();
        if (promotion.getStatus() != Promotion.PromotionStatus.active) {
            throw new IllegalArgumentException("Promotion is not active");
        }

        if (now.isBefore(promotion.getStartDate())) {
            throw new IllegalArgumentException("Promotion has not started yet");
        }

        if (now.isAfter(promotion.getEndDate())) {
            throw new IllegalArgumentException("Promotion has already ended");
        }

        if (promotion.getUsageLimit() != null && promotion.getUsageCount() >= promotion.getUsageLimit()) {
            throw new IllegalArgumentException("Promotion usage limit exceeded");
        }

        return convertToDTO(promotion);
    }

    private PromotionDTO convertToDTO(Promotion promotion) {
        PromotionDTO dto = new PromotionDTO();
        dto.setId(promotion.getId());
        dto.setName(promotion.getName());
        dto.setDescription(promotion.getDescription());
        dto.setDiscountType(promotion.getDiscountType().name());
        dto.setDiscountValue(promotion.getDiscountValue());
        dto.setCode(promotion.getCode());
        dto.setMinimumOrder(promotion.getMinimumOrder());
        dto.setStartDate(promotion.getStartDate());
        dto.setEndDate(promotion.getEndDate());
        dto.setUsageLimit(promotion.getUsageLimit());
        dto.setUsageCount(promotion.getUsageCount());
        dto.setStatus(promotion.getStatus().name());
        dto.setCreatedAt(promotion.getCreatedAt());
        dto.setUpdatedAt(promotion.getUpdatedAt());

        List<CategoryDTO> categoryDTOs = promotion.getCategories().stream()
                .map(category -> {
                    CategoryDTO categoryDTO = new CategoryDTO();
                    categoryDTO.setId(category.getId());
                    categoryDTO.setName(category.getName());
                    return categoryDTO;
                })
                .collect(Collectors.toList());

        dto.setCategories(categoryDTOs);

        return dto;
    }
}
