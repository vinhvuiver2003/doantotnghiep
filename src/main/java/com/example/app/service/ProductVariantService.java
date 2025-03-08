package com.example.app.service;


import com.example.app.dto.ProductVariantDTO;
import java.util.List;

public interface ProductVariantService {
    List<ProductVariantDTO> getVariantsByProduct(Integer productId);

    ProductVariantDTO getVariantById(Integer id);

    List<ProductVariantDTO> getAvailableVariantsByProduct(Integer productId);

    ProductVariantDTO createVariant(ProductVariantDTO variantDTO);

    ProductVariantDTO updateVariant(Integer id, ProductVariantDTO variantDTO);

    ProductVariantDTO updateVariantStock(Integer id, Integer quantity);

    ProductVariantDTO updateVariantStatus(Integer id, String status);

    void deleteVariant(Integer id);

    List<ProductVariantDTO> getLowStockVariants(Integer threshold);
}