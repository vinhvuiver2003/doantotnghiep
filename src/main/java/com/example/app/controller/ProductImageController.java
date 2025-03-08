package com.example.app.controller;
import com.example.app.dto.ApiResponse;
import com.example.app.dto.ProductImageDTO;
import com.example.app.service.ProductImageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-images")
public class ProductImageController {

    private final ProductImageService productImageService;

    @Autowired
    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    /**
     * Lấy danh sách hình ảnh của một sản phẩm
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ProductImageDTO>>> getImagesByProduct(@PathVariable Integer productId) {
        List<ProductImageDTO> images = productImageService.getImagesByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Product images retrieved successfully", images));
    }

    /**
     * Lấy danh sách hình ảnh của một biến thể sản phẩm
     */
    @GetMapping("/variant/{variantId}")
    public ResponseEntity<ApiResponse<List<ProductImageDTO>>> getImagesByVariant(@PathVariable Integer variantId) {
        List<ProductImageDTO> images = productImageService.getImagesByVariant(variantId);
        return ResponseEntity.ok(ApiResponse.success("Variant images retrieved successfully", images));
    }

    /**
     * Lấy chi tiết một hình ảnh
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductImageDTO>> getImageById(@PathVariable Integer id) {
        ProductImageDTO image = productImageService.getImageById(id);
        return ResponseEntity.ok(ApiResponse.success("Image retrieved successfully", image));
    }

    /**
     * Tạo mới hình ảnh cho sản phẩm (chỉ ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageDTO>> createImage(@Valid @RequestBody ProductImageDTO imageDTO) {
        ProductImageDTO createdImage = productImageService.createImage(imageDTO);
        return new ResponseEntity<>(
                ApiResponse.success("Product image created successfully", createdImage),
                HttpStatus.CREATED);
    }

    /**
     * Cập nhật thông tin hình ảnh (chỉ ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageDTO>> updateImage(
            @PathVariable Integer id,
            @Valid @RequestBody ProductImageDTO imageDTO) {

        ProductImageDTO updatedImage = productImageService.updateImage(id, imageDTO);
        return ResponseEntity.ok(ApiResponse.success("Image updated successfully", updatedImage));
    }

    /**
     * Xóa hình ảnh (chỉ ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteImage(@PathVariable Integer id) {
        productImageService.deleteImage(id);
        return ResponseEntity.ok(ApiResponse.success("Image deleted successfully"));
    }

    /**
     * Xóa tất cả hình ảnh của một sản phẩm (chỉ ADMIN)
     */
    @DeleteMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteImagesByProduct(@PathVariable Integer productId) {
        productImageService.deleteImagesByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("All product images deleted successfully"));
    }

    /**
     * Xóa tất cả hình ảnh của một biến thể sản phẩm (chỉ ADMIN)
     */
    @DeleteMapping("/variant/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteImagesByVariant(@PathVariable Integer variantId) {
        productImageService.deleteImagesByVariant(variantId);
        return ResponseEntity.ok(ApiResponse.success("All variant images deleted successfully"));
    }

    /**
     * Sắp xếp lại thứ tự hiển thị các hình ảnh (chỉ ADMIN)
     */
    @PostMapping("/product/{productId}/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> reorderImages(
            @PathVariable Integer productId,
            @RequestBody List<Integer> imageIds) {

        productImageService.reorderImages(productId, imageIds);
        return ResponseEntity.ok(ApiResponse.success("Images reordered successfully"));
    }
}