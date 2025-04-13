package com.example.app.controller;
import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.PromotionDTO;
import com.example.app.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    @Autowired
    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PagedResponse<PromotionDTO>>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<PromotionDTO> promotions = promotionService.getAllPromotions(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ResponseWrapper.success("Promotions retrieved successfully", promotions));
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PromotionDTO>> getPromotionById(@PathVariable Integer id) {
        PromotionDTO promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Promotion retrieved successfully", promotion));
    }


    @GetMapping("/active")
    public ResponseEntity<ResponseWrapper<List<PromotionDTO>>> getActivePromotions() {
        List<PromotionDTO> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(ResponseWrapper.success("Active promotions retrieved successfully", promotions));
    }


    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ResponseWrapper<List<PromotionDTO>>> getPromotionsByCategory(@PathVariable Integer categoryId) {
        List<PromotionDTO> promotions = promotionService.getPromotionsByCategory(categoryId);
        return ResponseEntity.ok(ResponseWrapper.success("Category promotions retrieved successfully", promotions));
    }


    @GetMapping("/validate-code")
    public ResponseEntity<ResponseWrapper<PromotionDTO>> validatePromotionCode(@RequestParam String code) {
        try {
            PromotionDTO promotion = promotionService.validatePromotion(code);
            return ResponseEntity.ok(ResponseWrapper.success("Promotion code is valid", promotion));
        } catch (Exception e) {
            // Tạo đối tượng ResponseWrapper<PromotionDTO> với data là null
            ResponseWrapper<PromotionDTO> response = new ResponseWrapper<>(false, e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PromotionDTO>> createPromotion(@Valid @RequestBody PromotionDTO promotionDTO) {
        PromotionDTO createdPromotion = promotionService.createPromotion(promotionDTO);
        return new ResponseEntity<>(
                ResponseWrapper.success("Promotion created successfully", createdPromotion),
                HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PromotionDTO>> updatePromotion(
            @PathVariable Integer id,
            @Valid @RequestBody PromotionDTO promotionDTO) {

        PromotionDTO updatedPromotion = promotionService.updatePromotion(id, promotionDTO);
        return ResponseEntity.ok(ResponseWrapper.success("Promotion updated successfully", updatedPromotion));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<?>> deletePromotion(@PathVariable Integer id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(ResponseWrapper.success("Promotion deleted successfully"));
    }


    @GetMapping("/code/{code}")
    public ResponseEntity<ResponseWrapper<PromotionDTO>> getPromotionByCode(@PathVariable String code) {
        PromotionDTO promotion = promotionService.getPromotionByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success("Promotion retrieved successfully", promotion));
    }
}