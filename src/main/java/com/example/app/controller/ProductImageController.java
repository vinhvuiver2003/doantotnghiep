package com.example.app.controller;
import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.ProductImageDTO;
import com.example.app.service.FileStorageService;
import com.example.app.service.ProductImageService;
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


    @PostMapping(value = "/upload/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<List<ProductImageDTO>>> uploadProductImages(
            @PathVariable Integer productId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "variantId", required = false) Integer variantId,
            @RequestParam(value = "isMainImage", required = false, defaultValue = "false") boolean isMainImage) {

        List<ProductImageDTO> uploadedImages = new ArrayList<>();

        try {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                String filePath = fileStorageService.storeProductImage(file, (long) productId, null);
                ProductImageDTO imageDTO = new ProductImageDTO();
                imageDTO.setProductId(productId);
                imageDTO.setVariantId(variantId);
                imageDTO.setImageURL(filePath);
                imageDTO.setSortOrder(i == 0 && isMainImage ? 0 : i + 1);

                ProductImageDTO savedImage = productImageService.createImage(imageDTO);
                uploadedImages.add(savedImage);
            }

            return new ResponseEntity<>(
                    ResponseWrapper.success("Images uploaded successfully", uploadedImages),
                    HttpStatus.CREATED
            );

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<List<ProductImageDTO>>(false, "Failed to upload images: " + e.getMessage(), null));
        }
    }


    @GetMapping("/product/{productId}/all")
    public ResponseEntity<ResponseWrapper<List<ProductImageDTO>>> getAllProductImages(@PathVariable Integer productId) {
        List<ProductImageDTO> allImages = productImageService.getAllProductImages(productId);
        return ResponseEntity.ok(ResponseWrapper.success("All product images retrieved successfully", allImages));
    }

    @PostMapping(value = "/upload/variant/{variantId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductImageDTO>> uploadVariantImage(
            @PathVariable Integer variantId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") Integer productId) {

        try {
            String filePath = fileStorageService.storeProductImage(file, (long) productId, "variant_" + variantId);

            ProductImageDTO imageDTO = new ProductImageDTO();
            imageDTO.setProductId(productId);
            imageDTO.setVariantId(variantId);
            imageDTO.setImageURL(filePath);
            imageDTO.setSortOrder(0); // Hình chính của biến thể

            ProductImageDTO savedImage = productImageService.createImage(imageDTO);

            return new ResponseEntity<>(
                    ResponseWrapper.success("Variant image uploaded successfully", savedImage),
                    HttpStatus.CREATED
            );


        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<ProductImageDTO>(false, "Failed to upload variant image: " + e.getMessage(), null));
        }
    }


    @DeleteMapping("/file/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<?>> deleteImageWithFile(@PathVariable Integer id) {
        try {
            ProductImageDTO image = productImageService.getImageById(id);
            fileStorageService.deleteFile(image.getImageURL());
            productImageService.deleteImage(id);
            return ResponseEntity.ok(ResponseWrapper.success("Image and file deleted successfully"));
        } catch (IOException e) {
            return new ResponseEntity<>(
                    ResponseWrapper.error("Failed to delete image file: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}