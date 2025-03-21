package com.example.app.controller;
import com.example.app.dto.ApiResponse;
import com.example.app.dto.ProductImageDTO;
import com.example.app.service.FileStorageService;
import com.example.app.service.ProductImageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/product-images")
public class ProductImageController {

    private final ProductImageService productImageService;
    private final FileStorageService fileStorageService;

    @Value("${app.file.access-path}")
    private String fileAccessPath;

    @Autowired
    public ProductImageController(ProductImageService productImageService, FileStorageService fileStorageService) {
        this.productImageService = productImageService;
        this.fileStorageService = fileStorageService;
    }



    /**
     * Upload hình ảnh cho sản phẩm (chỉ ADMIN)
     */
    @PostMapping(value = "/upload/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductImageDTO>>> uploadProductImages(
            @PathVariable Integer productId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "variantId", required = false) Integer variantId,
            @RequestParam(value = "isMainImage", required = false, defaultValue = "false") boolean isMainImage) {

        List<ProductImageDTO> uploadedImages = new ArrayList<>();

        try {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                // Lưu file và lấy đường dẫn
                String filePath = fileStorageService.storeProductImage(file, (long) productId, null);
                // Tạo DTO
                ProductImageDTO imageDTO = new ProductImageDTO();
                imageDTO.setProductId(productId);
                imageDTO.setVariantId(variantId);
                imageDTO.setImageURL(filePath);
                imageDTO.setSortOrder(i == 0 && isMainImage ? 0 : i + 1);

                // Lưu vào database
                ProductImageDTO savedImage = productImageService.createImage(imageDTO);
                uploadedImages.add(savedImage);
            }

            return new ResponseEntity<>(
                    ApiResponse.success("Images uploaded successfully", uploadedImages),
                    HttpStatus.CREATED
            );

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<List<ProductImageDTO>>(false, "Failed to upload images: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy tất cả hình ảnh của sản phẩm kèm thông tin biến thể
     */
    @GetMapping("/product/{productId}/all")
    public ResponseEntity<ApiResponse<List<ProductImageDTO>>> getAllProductImages(@PathVariable Integer productId) {
        List<ProductImageDTO> allImages = productImageService.getAllProductImages(productId);
        return ResponseEntity.ok(ApiResponse.success("All product images retrieved successfully", allImages));
    }

    /**
     * Upload một hình ảnh cho biến thể sản phẩm (chỉ ADMIN)
     */
    @PostMapping(value = "/upload/variant/{variantId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductImageDTO>> uploadVariantImage(
            @PathVariable Integer variantId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") Integer productId) {

        try {
            // Lưu file và lấy đường dẫn
            String filePath = fileStorageService.storeProductImage(file, (long) productId, "variant_" + variantId);

            // Tạo DTO
            ProductImageDTO imageDTO = new ProductImageDTO();
            imageDTO.setProductId(productId);
            imageDTO.setVariantId(variantId);
            imageDTO.setImageURL(filePath);
            imageDTO.setSortOrder(0); // Hình chính của biến thể

            // Lưu vào database
            ProductImageDTO savedImage = productImageService.createImage(imageDTO);

            return new ResponseEntity<>(
                    ApiResponse.success("Variant image uploaded successfully", savedImage),
                    HttpStatus.CREATED
            );

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<ProductImageDTO>(false, "Failed to upload variant image: " + e.getMessage(), null));
        }
    }

    /**
     * Xóa hình ảnh kèm theo xóa file vật lý (chỉ ADMIN)
     */
    @DeleteMapping("/file/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteImageWithFile(@PathVariable Integer id) {
        try {
            ProductImageDTO image = productImageService.getImageById(id);
            // Xóa file vật lý
            fileStorageService.deleteFile(image.getImageURL());
            // Xóa record trong database
            productImageService.deleteImage(id);
            return ResponseEntity.ok(ApiResponse.success("Image and file deleted successfully"));
        } catch (IOException e) {
            return new ResponseEntity<>(
                    ApiResponse.error("Failed to delete image file: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}