package com.example.app.service.impl;

import com.example.app.dto.RelatedProductDTO;
import com.example.app.entity.Product;
import com.example.app.entity.ProductImage;
import com.example.app.entity.ProductVariant;
import com.example.app.entity.RelatedProduct;
import com.example.app.entity.RelatedProductId;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.RelatedProductRepository;
import com.example.app.service.RelatedProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RelatedProductServiceImpl implements RelatedProductService {

    private final RelatedProductRepository relatedProductRepository;
    private final ProductRepository productRepository;

    @Autowired
    public RelatedProductServiceImpl(
            RelatedProductRepository relatedProductRepository,
            ProductRepository productRepository) {
        this.relatedProductRepository = relatedProductRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<RelatedProductDTO> getRelatedProducts(Integer productId) {
        // Kiểm tra sản phẩm tồn tại
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        List<RelatedProduct> relatedProducts = relatedProductRepository.findByProductId(productId);
        return relatedProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RelatedProductDTO> getRelatedProductsByType(Integer productId, RelatedProduct.RelationType relationType) {
        // Kiểm tra sản phẩm tồn tại
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        List<RelatedProduct> relatedProducts = relatedProductRepository.findByProductIdAndRelationType(productId, relationType);
        return relatedProducts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RelatedProductDTO addRelatedProduct(RelatedProductDTO relatedProductDTO) {
        // Kiểm tra sản phẩm chính tồn tại
        Product product = productRepository.findById(relatedProductDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + relatedProductDTO.getProductId()));

        // Kiểm tra sản phẩm liên quan tồn tại
        Product relatedProduct = productRepository.findById(relatedProductDTO.getRelatedProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Related product not found with id: " + relatedProductDTO.getRelatedProductId()));

        // Kiểm tra loại quan hệ hợp lệ
        RelatedProduct.RelationType relationType;
        try {
            relationType = RelatedProduct.RelationType.valueOf(relatedProductDTO.getRelationType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid relation type: " + relatedProductDTO.getRelationType());
        }

        // Tạo khóa chính tổng hợp
        RelatedProductId id = new RelatedProductId(
                product.getId(),
                relatedProduct.getId(),
                relationType
        );

        // Kiểm tra xem quan hệ này đã tồn tại chưa
        Optional<RelatedProduct> existingRelation = relatedProductRepository.findById(id);
        if (existingRelation.isPresent()) {
            throw new IllegalArgumentException("This relation already exists");
        }

        // Tạo mối quan hệ mới
        RelatedProduct newRelation = new RelatedProduct();
        newRelation.setId(id);
        newRelation.setProduct(product);
        newRelation.setRelatedProduct(relatedProduct);
        newRelation.setRelationType(relationType);

        RelatedProduct savedRelation = relatedProductRepository.save(newRelation);
        return convertToDTO(savedRelation);
    }

    @Override
    @Transactional
    public RelatedProductDTO updateRelationType(Integer productId, Integer relatedProductId, RelatedProduct.RelationType newRelationType) {
        // Kiểm tra sản phẩm chính tồn tại
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Kiểm tra sản phẩm liên quan tồn tại
        Product relatedProduct = productRepository.findById(relatedProductId)
                .orElseThrow(() -> new ResourceNotFoundException("Related product not found with id: " + relatedProductId));

        // Tìm tất cả mối quan hệ hiện có giữa 2 sản phẩm này
        List<RelatedProduct> existingRelations = relatedProductRepository.findByProductId(productId).stream()
                .filter(rp -> rp.getRelatedProduct().getId().equals(relatedProductId))
                .collect(Collectors.toList());

        if (existingRelations.isEmpty()) {
            throw new ResourceNotFoundException("No relation found between these products");
        }

        // Xóa tất cả mối quan hệ hiện có giữa 2 sản phẩm
        for (RelatedProduct relation : existingRelations) {
            relatedProductRepository.delete(relation);
        }

        // Tạo mối quan hệ mới với loại mới
        RelatedProductId id = new RelatedProductId(productId, relatedProductId, newRelationType);
        RelatedProduct newRelation = new RelatedProduct();
        newRelation.setId(id);
        newRelation.setProduct(product);
        newRelation.setRelatedProduct(relatedProduct);
        newRelation.setRelationType(newRelationType);

        RelatedProduct savedRelation = relatedProductRepository.save(newRelation);
        return convertToDTO(savedRelation);
    }

    @Override
    @Transactional
    public void removeRelatedProduct(Integer productId, Integer relatedProductId, RelatedProduct.RelationType relationType) {
        // Tạo ID của mối quan hệ
        RelatedProductId id = new RelatedProductId(productId, relatedProductId, relationType);

        // Kiểm tra xem mối quan hệ có tồn tại không
        if (!relatedProductRepository.existsById(id)) {
            throw new ResourceNotFoundException("Relation not found");
        }

        // Xóa mối quan hệ
        relatedProductRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void removeAllRelatedProducts(Integer productId) {
        // Kiểm tra sản phẩm tồn tại
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        // Xóa tất cả mối quan hệ từ sản phẩm này đến các sản phẩm khác
        relatedProductRepository.deleteByProductId(productId);

        // Xóa tất cả mối quan hệ từ các sản phẩm khác đến sản phẩm này
        relatedProductRepository.deleteByRelatedProductId(productId);
    }

    @Override
    public List<RelatedProductDTO> getSuggestedRelatedProducts(Integer productId, int limit) {
        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Lấy danh sách sản phẩm đã được thiết lập là liên quan
        List<RelatedProduct> existingRelations = relatedProductRepository.findByProductId(productId);
        List<Integer> existingRelatedIds = existingRelations.stream()
                .map(rp -> rp.getRelatedProduct().getId())
                .collect(Collectors.toList());

        // Thêm ID của sản phẩm hiện tại để loại trừ khỏi gợi ý
        existingRelatedIds.add(productId);

        // Tìm các sản phẩm cùng danh mục
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> sameCategoryProducts = productRepository.findByCategoryId(product.getCategory().getId(), pageable)
                .getContent().stream()
                .filter(p -> !existingRelatedIds.contains(p.getId()))
                .collect(Collectors.toList());

        // Nếu không đủ limit, tìm thêm các sản phẩm cùng thương hiệu
        List<Product> sameBrandProducts = new ArrayList<>();
        if (sameCategoryProducts.size() < limit) {
            sameBrandProducts = productRepository.findByBrandId(product.getBrand().getId(),
                            PageRequest.of(0, limit - sameCategoryProducts.size()))
                    .getContent().stream()
                    .filter(p -> !existingRelatedIds.contains(p.getId()) &&
                            !sameCategoryProducts.contains(p))
                    .collect(Collectors.toList());
        }

        // Kết hợp danh sách sản phẩm gợi ý
        List<Product> suggestedProducts = new ArrayList<>(sameCategoryProducts);
        suggestedProducts.addAll(sameBrandProducts);

        // Giới hạn số lượng theo yêu cầu
        if (suggestedProducts.size() > limit) {
            suggestedProducts = suggestedProducts.subList(0, limit);
        }

        // Chuyển đổi sang DTO
        return suggestedProducts.stream()
                .map(p -> createSuggestedRelationDTO(product, p))
                .collect(Collectors.toList());
    }

    // Tạo DTO cho mối quan hệ gợi ý (chưa lưu vào DB)
    private RelatedProductDTO createSuggestedRelationDTO(Product product, Product relatedProduct) {
        RelatedProductDTO dto = new RelatedProductDTO();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setProductImage(product.getMainImageUrl());
        dto.setProductPrice(product.getBasePrice());

        dto.setRelatedProductId(relatedProduct.getId());
        dto.setRelatedProductName(relatedProduct.getName());
        dto.setRelatedProductImage(relatedProduct.getMainImageUrl());
        dto.setRelatedProductPrice(relatedProduct.getBasePrice());

        // Xác định loại quan hệ gợi ý dựa trên thuộc tính sản phẩm
        if (product.getCategory().equals(relatedProduct.getCategory())) {
            dto.setRelationType(RelatedProduct.RelationType.similar.name());
        } else if (product.getBasePrice().compareTo(relatedProduct.getBasePrice()) < 0) {
            dto.setRelationType(RelatedProduct.RelationType.upsell.name());
        } else {
            dto.setRelationType(RelatedProduct.RelationType.cross_sell.name());
        }

        return dto;
    }

    // Utility method to convert Entity to DTO
    private RelatedProductDTO convertToDTO(RelatedProduct relatedProduct) {
        RelatedProductDTO dto = new RelatedProductDTO();

        Product product = relatedProduct.getProduct();
        Product related = relatedProduct.getRelatedProduct();

        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setProductImage(product.getMainImageUrl());
        dto.setProductPrice(product.getBasePrice());

        dto.setRelatedProductId(related.getId());
        dto.setRelatedProductName(related.getName());
        dto.setRelatedProductImage(related.getMainImageUrl());
        dto.setRelatedProductPrice(related.getBasePrice());

        dto.setRelationType(relatedProduct.getRelationType().name());

        return dto;
    }
}
