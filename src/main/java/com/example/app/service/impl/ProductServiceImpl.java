package com.example.app.service.impl;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.ProductDTO;
import com.example.app.dto.ProductVariantDTO;
import com.example.app.entity.*;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.*;
import com.example.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository productImageRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository productImageRepository,
            ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.variantRepository = variantRepository;
        this.productImageRepository = productImageRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public PagedResponse<ProductDTO> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productRepository.findAll(pageable);

        List<ProductDTO> content = products.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
    }

    @Override
    public ProductDTO getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return convertToDTO(product);
    }

    @Override
    public PagedResponse<ProductDTO> getProductsByCategory(Integer categoryId, int page, int size) {
        // Check if category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);

        List<ProductDTO> content = products.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
    }

    @Override
    public PagedResponse<ProductDTO> getProductsByBrand(Integer brandId, int page, int size) {
        // Check if brand exists
        if (!brandRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("Brand not found with id: " + brandId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByBrandId(brandId, pageable);

        List<ProductDTO> content = products.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
    }

    @Override
    public PagedResponse<ProductDTO> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.searchProducts(keyword, pageable);

        List<ProductDTO> content = products.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
    }

    @Override
    public PagedResponse<ProductDTO> filterProductsByPrice(BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByBasePriceBetween(minPrice, maxPrice, pageable);

        List<ProductDTO> content = products.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isLast()
        );
    }

    @Override
    public List<ProductDTO> getNewArrivals(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findNewArrivals(pageable);

        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getTopRatedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findTopRatedProducts(pageable);

        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getLowStockProducts(int threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);

        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        // Validate category and brand exist
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));

        Brand brand = brandRepository.findById(productDTO.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + productDTO.getBrandId()));

        // Create and save product
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice());
        product.setCategory(category);
        product.setBrand(brand);
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setImage(productDTO.getImage());

        if (productDTO.getStatus() != null) {
            product.setStatus(Product.ProductStatus.valueOf(productDTO.getStatus()));
        }

        Product savedProduct = productRepository.save(product);

        // Handle variants if provided
        if (productDTO.getVariants() != null && !productDTO.getVariants().isEmpty()) {
            for (ProductVariantDTO variantDTO : productDTO.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setProduct(savedProduct);
                variant.setColor(variantDTO.getColor());
                variant.setSize(variantDTO.getSize());
                variant.setStockQuantity(variantDTO.getStockQuantity());
                variant.setPriceAdjustment(variantDTO.getPriceAdjustment());
                variant.setImage(variantDTO.getImage());

                if (variantDTO.getStatus() != null) {
                    variant.setStatus(ProductVariant.VariantStatus.valueOf(variantDTO.getStatus()));
                }

                variantRepository.save(variant);
            }
        }

        // Handle images if provided
        if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
            int sortOrder = 0;
            for (String imageUrl : productDTO.getImages()) {
                ProductImage image = new ProductImage();
                image.setProduct(savedProduct);
                image.setImageURL(imageUrl);
                image.setSortOrder(sortOrder++);

                productImageRepository.save(image);
            }
        }

        return convertToDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Integer id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Update basic product information
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setImage(productDTO.getImage());

        if (productDTO.getStatus() != null) {
            product.setStatus(Product.ProductStatus.valueOf(productDTO.getStatus()));
        }

        // Update category if changed
        if (productDTO.getCategoryId() != null &&
                (product.getCategory() == null || !product.getCategory().getId().equals(productDTO.getCategoryId()))) {

            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }

        // Update brand if changed
        if (productDTO.getBrandId() != null &&
                (product.getBrand() == null || !product.getBrand().getId().equals(productDTO.getBrandId()))) {

            Brand brand = brandRepository.findById(productDTO.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + productDTO.getBrandId()));
            product.setBrand(brand);
        }

        Product updatedProduct = productRepository.save(product);

        // Handle variants if provided
        if (productDTO.getVariants() != null) {
            // Advanced variant handling would require more complex logic to update, add, or delete variants
            // This is a simplified implementation
            for (ProductVariantDTO variantDTO : productDTO.getVariants()) {
                if (variantDTO.getId() != null) {
                    // Update existing variant
                    ProductVariant variant = variantRepository.findById(variantDTO.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantDTO.getId()));

                    variant.setColor(variantDTO.getColor());
                    variant.setSize(variantDTO.getSize());
                    variant.setStockQuantity(variantDTO.getStockQuantity());
                    variant.setPriceAdjustment(variantDTO.getPriceAdjustment());
                    variant.setImage(variantDTO.getImage());

                    if (variantDTO.getStatus() != null) {
                        variant.setStatus(ProductVariant.VariantStatus.valueOf(variantDTO.getStatus()));
                    }

                    variantRepository.save(variant);
                } else {
                    // Create new variant
                    ProductVariant variant = new ProductVariant();
                    variant.setProduct(updatedProduct);
                    variant.setColor(variantDTO.getColor());
                    variant.setSize(variantDTO.getSize());
                    variant.setStockQuantity(variantDTO.getStockQuantity());
                    variant.setPriceAdjustment(variantDTO.getPriceAdjustment());
                    variant.setImage(variantDTO.getImage());

                    if (variantDTO.getStatus() != null) {
                        variant.setStatus(ProductVariant.VariantStatus.valueOf(variantDTO.getStatus()));
                    }

                    variantRepository.save(variant);
                }
            }
        }

        // Handle product images
        // For simplicity, we're not implementing image update here
        // A complete implementation would manage adding/removing/reordering images

        return convertToDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        // Check if product exists
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        // Delete all associated entities
        productImageRepository.deleteByProductId(id);

        // Delete the product (which will cascade delete variants due to relationship setup)
        productRepository.deleteById(id);
    }

    @Override
    public Long countProducts() {
        return productRepository.count();
    }

    // Utility method to convert Entity to DTO
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setBasePrice(product.getBasePrice());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getBrand() != null) {
            dto.setBrandId(product.getBrand().getId());
            dto.setBrandName(product.getBrand().getName());
        }

        dto.setStockQuantity(product.getStockQuantity());
        dto.setImage(product.getImage());
        dto.setStatus(product.getStatus().name());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        // Get variants
        List<ProductVariantDTO> variantDTOs = product.getVariants().stream()
                .map(variant -> {
                    ProductVariantDTO variantDTO = new ProductVariantDTO();
                    variantDTO.setId(variant.getId());
                    variantDTO.setProductId(product.getId());
                    variantDTO.setColor(variant.getColor());
                    variantDTO.setSize(variant.getSize());
                    variantDTO.setStockQuantity(variant.getStockQuantity());
                    variantDTO.setPriceAdjustment(variant.getPriceAdjustment());
                    variantDTO.setFinalPrice(product.getBasePrice().add(variant.getPriceAdjustment()));
                    variantDTO.setImage(variant.getImage());
                    variantDTO.setStatus(variant.getStatus().name());

                    // Get variant images - Sử dụng productImageRepository để lấy ảnh theo variantId
                    List<String> variantImages = productImageRepository.findByVariantId(variant.getId())
                            .stream()
                            .map(ProductImage::getImageURL)
                            .collect(Collectors.toList());
                    variantDTO.setImages(variantImages);

                    return variantDTO;
                })
                .collect(Collectors.toList());
        dto.setVariants(variantDTOs);

        // Get product images
        List<String> imageUrls = product.getImages().stream()
                .sorted((img1, img2) -> img1.getSortOrder().compareTo(img2.getSortOrder()))
                .map(ProductImage::getImageURL)
                .collect(Collectors.toList());
        dto.setImages(imageUrls);

        // Calculate average rating
        Double avgRating = reviewRepository.calculateAverageRating(product.getId());
        dto.setAverageRating(avgRating != null ? avgRating : 0.0);

        // Count reviews
        dto.setReviewCount((long) product.getReviews().size());

        return dto;
    }
}