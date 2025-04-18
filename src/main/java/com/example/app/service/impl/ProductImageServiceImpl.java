package com.example.app.service.impl;

import com.example.app.dto.ProductImageDTO;
import com.example.app.entity.Product;
import com.example.app.entity.ProductImage;
import com.example.app.entity.ProductVariant;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.ProductImageRepository;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.ProductVariantRepository;
import com.example.app.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    @Autowired
    public ProductImageServiceImpl(
            ProductImageRepository productImageRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    public List<ProductImageDTO> getImagesByProduct(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrder(productId);

        return images.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductImageDTO> getImagesByVariant(Integer variantId) {
        if (!variantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("Product variant not found with id: " + variantId);
        }

        List<ProductImage> images = productImageRepository.findByVariantId(variantId);

        return images.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductImageDTO getImageById(Integer id) {
        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + id));

        return convertToDTO(image);
    }

    @Override
    @Transactional
    public ProductImageDTO createImage(ProductImageDTO imageDTO) {
        Product product = productRepository.findById(imageDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + imageDTO.getProductId()));

        ProductVariant variant = null;
        if (imageDTO.getVariantId() != null) {
            variant = variantRepository.findById(imageDTO.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + imageDTO.getVariantId()));

            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new IllegalArgumentException("Variant does not belong to the specified product");
            }
        }

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setVariant(variant);
        image.setImageURL(imageDTO.getImageURL());

        if (imageDTO.getSortOrder() != null) {
            image.setSortOrder(imageDTO.getSortOrder());
        } else {
            List<ProductImage> existingImages = productImageRepository.findByProductIdOrderBySortOrder(product.getId());
            image.setSortOrder(existingImages.size());
        }

        ProductImage savedImage = productImageRepository.save(image);

        return convertToDTO(savedImage);
    }

    @Override
    @Transactional
    public ProductImageDTO updateImage(Integer id, ProductImageDTO imageDTO) {
        ProductImage image = productImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found with id: " + id));

        if (imageDTO.getVariantId() != null &&
                (image.getVariant() == null || !image.getVariant().getId().equals(imageDTO.getVariantId()))) {

            ProductVariant variant = variantRepository.findById(imageDTO.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + imageDTO.getVariantId()));

            if (!variant.getProduct().getId().equals(image.getProduct().getId())) {
                throw new IllegalArgumentException("Variant does not belong to the product of this image");
            }

            image.setVariant(variant);
        } else if (imageDTO.getVariantId() == null && image.getVariant() != null) {
            image.setVariant(null);
        }

        if (imageDTO.getImageURL() != null && !imageDTO.getImageURL().isEmpty()) {
            image.setImageURL(imageDTO.getImageURL());
        }

        if (imageDTO.getSortOrder() != null) {
            image.setSortOrder(imageDTO.getSortOrder());
        }

        ProductImage updatedImage = productImageRepository.save(image);

        return convertToDTO(updatedImage);
    }

    @Override
    @Transactional
    public void deleteImage(Integer id) {
        if (!productImageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product image not found with id: " + id);
        }

        productImageRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteImagesByProduct(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        productImageRepository.deleteByProductId(productId);
    }

    @Override
    @Transactional
    public void deleteImagesByVariant(Integer variantId) {
        if (!variantRepository.existsById(variantId)) {
            throw new ResourceNotFoundException("Product variant not found with id: " + variantId);
        }

        productImageRepository.deleteByVariantId(variantId);
    }

    @Override
    @Transactional
    public void reorderImages(Integer productId, List<Integer> imageIds) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        List<ProductImage> images = productImageRepository.findByProductId(productId);
        Map<Integer, ProductImage> imageMap = images.stream()
                .collect(Collectors.toMap(ProductImage::getId, Function.identity()));

        for (Integer imageId : imageIds) {
            if (!imageMap.containsKey(imageId)) {
                throw new ResourceNotFoundException("Product image not found with id " + imageId + " or does not belong to product " + productId);
            }
        }

        IntStream.range(0, imageIds.size()).forEach(index -> {
            Integer imageId = imageIds.get(index);
            ProductImage image = imageMap.get(imageId);
            image.setSortOrder(index);
            productImageRepository.save(image);
        });
    }
    @Override
    public List<ProductImageDTO> getAllProductImages(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrder(productId);

        return images.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProductImageDTO convertToDTO(ProductImage image) {
        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(image.getId());
        dto.setProductId(image.getProduct().getId());

        if (image.getVariant() != null) {
            dto.setVariantId(image.getVariant().getId());
        }

        dto.setImageURL(image.getImageURL());
        dto.setSortOrder(image.getSortOrder());
        dto.setCreatedAt(image.getCreatedAt());

        return dto;
    }
}
