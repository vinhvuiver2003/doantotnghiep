package com.example.app.service;

import com.example.app.dto.PagedResponse;
import com.example.app.dto.PromotionDTO;

import java.util.List;

public interface PromotionService {
    PagedResponse<PromotionDTO> getAllPromotions(int page, int size, String sortBy, String sortDir);

    PromotionDTO getPromotionById(Integer id);

    PromotionDTO getPromotionByCode(String code);

    PromotionDTO createPromotion(PromotionDTO promotionDTO);

    PromotionDTO updatePromotion(Integer id, PromotionDTO promotionDTO);

    void deletePromotion(Integer id);

    List<PromotionDTO> getActivePromotions();

    List<PromotionDTO> getPromotionsByCategory(Integer categoryId);

    PromotionDTO validatePromotion(String code);
}