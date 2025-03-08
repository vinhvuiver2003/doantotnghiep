package com.example.app.controller;
import com.example.app.dto.ApiResponse;
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

    /**
     * Lấy danh sách khuyến mãi có phân trang (chỉ ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<PromotionDTO>>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<PromotionDTO> promotions = promotionService.getAllPromotions(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Promotions retrieved successfully", promotions));
    }

    /**
     * Lấy thông tin chi tiết một khuyến mãi theo ID (chỉ ADMIN)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionDTO>> getPromotionById(@PathVariable Integer id) {
        PromotionDTO promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(ApiResponse.success("Promotion retrieved successfully", promotion));
    }

    /**
     * Lấy danh sách khuyến mãi đang hoạt động (cho trang chủ)
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromotionDTO>>> getActivePromotions() {
        List<PromotionDTO> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(ApiResponse.success("Active promotions retrieved successfully", promotions));
    }

    /**
     * Lấy danh sách khuyến mãi theo danh mục
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<PromotionDTO>>> getPromotionsByCategory(@PathVariable Integer categoryId) {
        List<PromotionDTO> promotions = promotionService.getPromotionsByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category promotions retrieved successfully", promotions));
    }

    /**
     * Kiểm tra mã giảm giá có hợp lệ không
     */
    @GetMapping("/validate-code")
    public ResponseEntity<ApiResponse<PromotionDTO>> validatePromotionCode(@RequestParam String code) {
        try {
            PromotionDTO promotion = promotionService.validatePromotion(code);
            return ResponseEntity.ok(ApiResponse.success("Promotion code is valid", promotion));
        } catch (Exception e) {
            // Tạo đối tượng ApiResponse<PromotionDTO> với data là null
            ApiResponse<PromotionDTO> response = new ApiResponse<>(false, e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Tạo mới một khuyến mãi (chỉ ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionDTO>> createPromotion(@Valid @RequestBody PromotionDTO promotionDTO) {
        PromotionDTO createdPromotion = promotionService.createPromotion(promotionDTO);
        return new ResponseEntity<>(
                ApiResponse.success("Promotion created successfully", createdPromotion),
                HttpStatus.CREATED);
    }

    /**
     * Cập nhật thông tin khuyến mãi (chỉ ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionDTO>> updatePromotion(
            @PathVariable Integer id,
            @Valid @RequestBody PromotionDTO promotionDTO) {

        PromotionDTO updatedPromotion = promotionService.updatePromotion(id, promotionDTO);
        return ResponseEntity.ok(ApiResponse.success("Promotion updated successfully", updatedPromotion));
    }

    /**
     * Xóa một khuyến mãi (chỉ ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deletePromotion(@PathVariable Integer id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(ApiResponse.success("Promotion deleted successfully"));
    }

    /**
     * Lấy khuyến mãi theo mã code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<PromotionDTO>> getPromotionByCode(@PathVariable String code) {
        PromotionDTO promotion = promotionService.getPromotionByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Promotion retrieved successfully", promotion));
    }
}