package com.example.app.service;

import com.example.app.dto.RelatedProductDTO;
import com.example.app.entity.RelatedProduct;

import java.util.List;

public interface RelatedProductService {
    List<RelatedProductDTO> getRelatedProducts(Integer productId);

    List<RelatedProductDTO> getRelatedProductsByType(Integer productId, RelatedProduct.RelationType relationType);

    RelatedProductDTO addRelatedProduct(RelatedProductDTO relatedProductDTO);

    RelatedProductDTO updateRelationType(Integer productId, Integer relatedProductId, RelatedProduct.RelationType relationType);

    void removeRelatedProduct(Integer productId, Integer relatedProductId, RelatedProduct.RelationType relationType);

    void removeAllRelatedProducts(Integer productId);

    List<RelatedProductDTO> getSuggestedRelatedProducts(Integer productId, int limit);
}