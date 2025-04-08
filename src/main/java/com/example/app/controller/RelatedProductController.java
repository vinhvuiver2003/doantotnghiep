package com.example.app.controller;

import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.RelatedProductDTO;
import com.example.app.entity.RelatedProduct;
import com.example.app.service.RelatedProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/related-products")
public class RelatedProductController {

    private final RelatedProductService relatedProductService;

    @Autowired
    public RelatedProductController(RelatedProductService relatedProductService) {
        this.relatedProductService = relatedProductService;
    }

    /**
     * Lấy danh sách sản phẩm liên quan của một sản phẩm
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ResponseWrapper<List<RelatedProductDTO>>> getRelatedProducts(@PathVariable Integer productId) {
        List<RelatedProductDTO> relatedProducts = relatedProductService.getRelatedProducts(productId);
        return ResponseEntity.ok(ResponseWrapper.success("Related products retrieved successfully", relatedProducts));
    }

    /**
     * Lấy danh sách sản phẩm liên quan theo loại liên quan (upsell, cross_sell, accessory, similar)
     */
    @GetMapping("/product/{productId}/type/{relationType}")
    public ResponseEntity<ResponseWrapper<List<RelatedProductDTO>>> getRelatedProductsByType(
            @PathVariable Integer productId,
            @PathVariable String relationType) {

        RelatedProduct.RelationType type = RelatedProduct.RelationType.valueOf(relationType);
        List<RelatedProductDTO> relatedProducts = relatedProductService.getRelatedProductsByType(productId, type);
        return ResponseEntity.ok(ResponseWrapper.success("Related products retrieved successfully", relatedProducts));
    }

    /**
     * Thêm mối quan hệ sản phẩm (chỉ ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<RelatedProductDTO>> addRelatedProduct(
            @Valid @RequestBody RelatedProductDTO relatedProductDTO) {

        RelatedProductDTO createdRelation = relatedProductService.addRelatedProduct(relatedProductDTO);
        return new ResponseEntity<>(
                ResponseWrapper.success("Related product added successfully", createdRelation),
                HttpStatus.CREATED);
    }

    /**
     * Cập nhật loại mối quan hệ (chỉ ADMIN)
     */
    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<RelatedProductDTO>> updateRelationType(
            @RequestParam Integer productId,
            @RequestParam Integer relatedProductId,
            @RequestParam String relationType) {

        RelatedProduct.RelationType type = RelatedProduct.RelationType.valueOf(relationType);
        RelatedProductDTO updatedRelation = relatedProductService.updateRelationType(productId, relatedProductId, type);
        return ResponseEntity.ok(ResponseWrapper.success("Relation type updated successfully", updatedRelation));
    }

    /**
     * Xóa mối quan hệ sản phẩm (chỉ ADMIN)
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<?>> removeRelatedProduct(
            @RequestParam Integer productId,
            @RequestParam Integer relatedProductId,
            @RequestParam String relationType) {

        RelatedProduct.RelationType type = RelatedProduct.RelationType.valueOf(relationType);
        relatedProductService.removeRelatedProduct(productId, relatedProductId, type);
        return ResponseEntity.ok(ResponseWrapper.success("Related product removed successfully"));
    }

    /**
     * Xóa tất cả mối quan hệ của một sản phẩm (chỉ ADMIN)
     */
    @DeleteMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<?>> removeAllRelatedProducts(@PathVariable Integer productId) {
        relatedProductService.removeAllRelatedProducts(productId);
        return ResponseEntity.ok(ResponseWrapper.success("All related products removed successfully"));
    }

    /**
     * Gợi ý sản phẩm liên quan dựa trên danh mục và thương hiệu
     */
    @GetMapping("/suggestions/product/{productId}")
    public ResponseEntity<ResponseWrapper<List<RelatedProductDTO>>> getSuggestedRelatedProducts(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "10") int limit) {

        List<RelatedProductDTO> suggestions = relatedProductService.getSuggestedRelatedProducts(productId, limit);
        return ResponseEntity.ok(ResponseWrapper.success("Suggested related products retrieved successfully", suggestions));
    }
}