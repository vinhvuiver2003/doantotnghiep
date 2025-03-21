package com.example.app.service;

import com.example.app.dto.ProductImageDTO;

import java.util.List;

public interface ProductImageService {
    List<ProductImageDTO> getImagesByProduct(Integer productId);

    List<ProductImageDTO> getImagesByVariant(Integer variantId);

    ProductImageDTO getImageById(Integer id);

    ProductImageDTO createImage(ProductImageDTO imageDTO);

    ProductImageDTO updateImage(Integer id, ProductImageDTO imageDTO);
    List<ProductImageDTO> getAllProductImages(Integer productId);
    void deleteImage(Integer id);

    void deleteImagesByProduct(Integer productId);

    void deleteImagesByVariant(Integer variantId);

    void reorderImages(Integer productId, List<Integer> imageIds);
}