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


    @GetMapping("/product/{productId}")
    public ResponseEntity<ResponseWrapper<List<ProductVariantDTO>>> getVariantsByProduct(@PathVariable Integer productId) {
        List<ProductVariantDTO> variants = variantService.getVariantsByProduct(productId);
        return ResponseEntity.ok(ResponseWrapper.success("Variants retrieved successfully", variants));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<ProductVariantDTO>> getVariantById(@PathVariable Integer id) {
        ProductVariantDTO variant = variantService.getVariantById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Variant retrieved successfully", variant));
    }


    @GetMapping("/product/{productId}/available")
    public ResponseEntity<ResponseWrapper<List<ProductVariantDTO>>> getAvailableVariantsByProduct(@PathVariable Integer productId) {
        List<ProductVariantDTO> variants = variantService.getAvailableVariantsByProduct(productId);
        return ResponseEntity.ok(ResponseWrapper.success("Available variants retrieved successfully", variants));
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductVariantDTO>> createVariant(@Valid @RequestBody ProductVariantDTO variantDTO) {
        ProductVariantDTO createdVariant = variantService.createVariant(variantDTO);
        return new ResponseEntity<>(
                ResponseWrapper.success("Variant created successfully", createdVariant),
                HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductVariantDTO>> updateVariant(
            @PathVariable Integer id,
            @Valid @RequestBody ProductVariantDTO variantDTO) {

        ProductVariantDTO updatedVariant = variantService.updateVariant(id, variantDTO);
        return ResponseEntity.ok(ResponseWrapper.success("Variant updated successfully", updatedVariant));
    }


    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductVariantDTO>> updateVariantStock(
            @PathVariable Integer id,
            @RequestParam Integer quantity) {

        ProductVariantDTO updatedVariant = variantService.updateVariantStock(id, quantity);
        return ResponseEntity.ok(ResponseWrapper.success("Variant stock updated successfully", updatedVariant));
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductVariantDTO>> updateVariantStatus(
            @PathVariable Integer id,
            @RequestParam String status) {

        ProductVariantDTO updatedVariant = variantService.updateVariantStatus(id, status);
        return ResponseEntity.ok(ResponseWrapper.success("Variant status updated successfully", updatedVariant));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<?>> deleteVariant(@PathVariable Integer id) {
        variantService.deleteVariant(id);
        return ResponseEntity.ok(ResponseWrapper.success("Variant deleted successfully"));
    }

   
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<List<ProductVariantDTO>>> getLowStockVariants(
            @RequestParam(defaultValue = "10") Integer threshold) {

        List<ProductVariantDTO> variants = variantService.getLowStockVariants(threshold);
        return ResponseEntity.ok(ResponseWrapper.success("Low stock variants retrieved successfully", variants));
    }
}