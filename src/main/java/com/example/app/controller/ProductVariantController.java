package com.example.app.controller;

import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.ProductVariantDTO;
import com.example.app.service.ProductVariantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
public class ProductVariantController {

    private final ProductVariantService variantService;

    @Autowired
    public ProductVariantController(ProductVariantService variantService) {
        this.variantService = variantService;
    }

    /**
     * Lấy danh sách biến thể theo sản phẩm
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ResponseWrapper<List<ProductVariantDTO>>> getVariantsByProduct(@PathVariable Integer productId) {
        List<ProductVariantDTO> variants = variantService.getVariantsByProduct(productId);
        return ResponseEntity.ok(ResponseWrapper.success("Variants retrieved successfully", variants));
    }

    /**
     * Lấy thông tin chi tiết một biến thể theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductVariantDTO>> getVariantById(@PathVariable Integer id) {
        ProductVariantDTO variant = variantService.getVariantById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Variant retrieved successfully", variant));
    }

    /**
     * Lấy danh sách biến thể có sẵn theo sản phẩm (có tồn kho > 0)
     */
    @GetMapping("/product/{productId}/available")
    public ResponseEntity<ResponseWrapper<List<ProductVariantDTO>>> getAvailableVariantsByProduct(@PathVariable Integer productId) {
        List<ProductVariantDTO> variants = variantService.getAvailableVariantsByProduct(productId);
        return ResponseEntity.ok(ResponseWrapper.success("Available variants retrieved successfully", variants));
    }

    /**
     * Tạo mới biến thể sản phẩm (chỉ ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductVariantDTO>> createVariant(@Valid @RequestBody ProductVariantDTO variantDTO) {
        ProductVariantDTO createdVariant = variantService.createVariant(variantDTO);
        return new ResponseEntity<>(
                ResponseWrapper.success("Variant created successfully", createdVariant),
                HttpStatus.CREATED);
    }

    /**
     * Cập nhật thông tin biến thể sản phẩm (chỉ ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductVariantDTO>> updateVariant(
            @PathVariable Integer id,
            @Valid @RequestBody ProductVariantDTO variantDTO) {

        ProductVariantDTO updatedVariant = variantService.updateVariant(id, variantDTO);
        return ResponseEntity.ok(ResponseWrapper.success("Variant updated successfully", updatedVariant));
    }

    /**
     * Cập nhật tồn kho biến thể sản phẩm (chỉ ADMIN)
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductVariantDTO>> updateVariantStock(
            @PathVariable Integer id,
            @RequestParam Integer quantity) {

        ProductVariantDTO updatedVariant = variantService.updateVariantStock(id, quantity);
        return ResponseEntity.ok(ResponseWrapper.success("Variant stock updated successfully", updatedVariant));
    }

    /**
     * Cập nhật trạng thái biến thể sản phẩm (chỉ ADMIN)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductVariantDTO>> updateVariantStatus(
            @PathVariable Integer id,
            @RequestParam String status) {

        ProductVariantDTO updatedVariant = variantService.updateVariantStatus(id, status);
        return ResponseEntity.ok(ResponseWrapper.success("Variant status updated successfully", updatedVariant));
    }

    /**
     * Xóa biến thể sản phẩm (chỉ ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<?>> deleteVariant(@PathVariable Integer id) {
        variantService.deleteVariant(id);
        return ResponseEntity.ok(ResponseWrapper.success("Variant deleted successfully"));
    }

    /**
     * Lấy danh sách biến thể có tồn kho thấp (chỉ ADMIN)
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<List<ProductVariantDTO>>> getLowStockVariants(
            @RequestParam(defaultValue = "10") Integer threshold) {

        List<ProductVariantDTO> variants = variantService.getLowStockVariants(threshold);
        return ResponseEntity.ok(ResponseWrapper.success("Low stock variants retrieved successfully", variants));
    }
}