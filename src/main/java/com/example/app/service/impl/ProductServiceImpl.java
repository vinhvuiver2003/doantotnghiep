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
import java.util.Set;
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
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Product> products = productRepository.findAll(pageable);
            
            List<ProductDTO> content = new ArrayList<>();
            
            for (Product product : products.getContent()) {
                try {
                    ProductDTO dto = convertToDTOSafe(product);
                    if (dto != null) {
                        content.add(dto);
                    } else {
                        System.err.println("Skipping null product DTO for product ID: " + 
                            (product != null ? product.getId() : "unknown"));
                    }
                } catch (Exception e) {
                    // Log error but continue with other products
                    System.err.println("Error converting product with ID " + 
                        (product != null ? product.getId() : "unknown") + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            return new PagedResponse<>(
                    content,
                    products.getNumber(),
                    products.getSize(),
                    products.getTotalElements(),
                    products.getTotalPages(),
                    products.isLast()
            );
        } catch (Exception e) {
            System.err.println("Error in getAllProducts: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error retrieving all products: " + e.getMessage(), e);
        }
    }

    @Override
    public ProductDTO getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return convertToDTOSafe(product);
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
                .map(this::convertToDTOSafe)
                .filter(dto -> dto != null)
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
                .map(this::convertToDTOSafe)
                .filter(dto -> dto != null)
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
                .map(this::convertToDTOSafe)
                .filter(dto -> dto != null)
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
                .map(this::convertToDTOSafe)
                .filter(dto -> dto != null)
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
                .map(this::convertToDTOSafe)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getTopRatedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findTopRatedProducts(pageable);

        return products.stream()
                .map(this::convertToDTOSafe)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getLowStockProducts(int threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);

        return products.stream()
                .map(this::convertToDTOSafe)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        // Validate input
        if (productDTO.getBasePrice() != null && productDTO.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá sản phẩm không được âm");
        }
        if (productDTO.getStockQuantity() != null && productDTO.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }

        // Validate category and brand exist
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));

        Brand brand = brandRepository.findById(productDTO.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + productDTO.getBrandId()));

        // Create and save product
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice() != null ? productDTO.getBasePrice() : BigDecimal.ZERO);
        product.setCategory(category);
        product.setBrand(brand);
        product.setStockQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : 0);
        product.setImage(productDTO.getImage());

        if (productDTO.getStatus() != null) {
            product.setStatus(Product.ProductStatus.valueOf(productDTO.getStatus()));
        }

        Product savedProduct = productRepository.save(product);

        // Handle variants if provided
        if (productDTO.getVariants() != null && !productDTO.getVariants().isEmpty()) {
            for (ProductVariantDTO variantDTO : productDTO.getVariants()) {
                // Validate variant
                if (variantDTO.getPriceAdjustment() != null && variantDTO.getPriceAdjustment().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Điều chỉnh giá biến thể không được âm");
                }
                if (variantDTO.getStockQuantity() != null && variantDTO.getStockQuantity() < 0) {
                    throw new IllegalArgumentException("Số lượng tồn kho biến thể không được âm");
                }

                ProductVariant variant = new ProductVariant();
                variant.setProduct(savedProduct);
                variant.setColor(variantDTO.getColor());
                variant.setSize(variantDTO.getSize());
                variant.setStockQuantity(variantDTO.getStockQuantity() != null ? variantDTO.getStockQuantity() : 0);
                variant.setPriceAdjustment(variantDTO.getPriceAdjustment() != null ? variantDTO.getPriceAdjustment() : BigDecimal.ZERO);
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

        return convertToDTOSafe(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Integer id, ProductDTO productDTO) {
        // Validate input
        if (productDTO.getBasePrice() != null && productDTO.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá sản phẩm không được âm");
        }
        if (productDTO.getStockQuantity() != null && productDTO.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Update basic product information
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice() != null ? productDTO.getBasePrice() : product.getBasePrice());
        product.setStockQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : product.getStockQuantity());
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
        if (productDTO.getVariants() != null && !productDTO.getVariants().isEmpty()) {
            for (ProductVariantDTO variantDTO : productDTO.getVariants()) {
                // Validate variant
                if (variantDTO.getPriceAdjustment() != null && variantDTO.getPriceAdjustment().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Điều chỉnh giá biến thể không được âm");
                }
                if (variantDTO.getStockQuantity() != null && variantDTO.getStockQuantity() < 0) {
                    throw new IllegalArgumentException("Số lượng tồn kho biến thể không được âm");
                }

                ProductVariant variant;
                if (variantDTO.getId() != null) {
                    variant = variantRepository.findById(variantDTO.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantDTO.getId()));
                    variant.setColor(variantDTO.getColor());
                    variant.setSize(variantDTO.getSize());
                    variant.setStockQuantity(variantDTO.getStockQuantity() != null ? variantDTO.getStockQuantity() : variant.getStockQuantity());
                    variant.setPriceAdjustment(variantDTO.getPriceAdjustment() != null ? variantDTO.getPriceAdjustment() : variant.getPriceAdjustment());
                    variant.setImage(variantDTO.getImage());
                } else {
                    variant = new ProductVariant();
                    variant.setProduct(updatedProduct);
                    variant.setColor(variantDTO.getColor());
                    variant.setSize(variantDTO.getSize());
                    variant.setStockQuantity(variantDTO.getStockQuantity() != null ? variantDTO.getStockQuantity() : 0);
                    variant.setPriceAdjustment(variantDTO.getPriceAdjustment() != null ? variantDTO.getPriceAdjustment() : BigDecimal.ZERO);
                    variant.setImage(variantDTO.getImage());
                }

                if (variantDTO.getStatus() != null) {
                    variant.setStatus(ProductVariant.VariantStatus.valueOf(variantDTO.getStatus()));
                }

                variantRepository.save(variant);
            }
        }

        // Handle images if provided
        if (productDTO.getImages() != null) {
            // Delete existing images
            productImageRepository.deleteByProductId(id);

            // Add new images
            if (!productDTO.getImages().isEmpty()) {
                int sortOrder = 0;
                for (String imageUrl : productDTO.getImages()) {
                    ProductImage image = new ProductImage();
                    image.setProduct(updatedProduct);
                    image.setImageURL(imageUrl);
                    image.setSortOrder(sortOrder++);

                    productImageRepository.save(image);
                }
            }
        }

        return convertToDTOSafe(updatedProduct);
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

    // Helper method for safe conversion
    private ProductDTO convertToDTOSafe(Product product) {
        if (product == null) {
            System.err.println("Cannot convert null product to DTO");
            return null;
        }
        
        try {
            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setName(product.getName() != null ? product.getName() : "");
            dto.setDescription(product.getDescription());
            dto.setBasePrice(product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO);

            if (product.getCategory() != null) {
                dto.setCategoryId(product.getCategory().getId());
                dto.setCategoryName(product.getCategory().getName());
            }

            if (product.getBrand() != null) {
                dto.setBrandId(product.getBrand().getId());
                dto.setBrandName(product.getBrand().getName());
            }

            dto.setStockQuantity(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
            dto.setImage(product.getImage());
            if (product.getStatus() != null) {
                dto.setStatus(product.getStatus().name());
            } else {
                dto.setStatus("active"); // Default value
            }
            dto.setCreatedAt(product.getCreatedAt());
            dto.setUpdatedAt(product.getUpdatedAt());

            // Khởi tạo danh sách rỗng cho variants
            List<ProductVariantDTO> variantDTOs = new ArrayList<>();
            
            // Sao chép an toàn danh sách variants
            List<ProductVariant> variantsList = safeCollectionCopy(
                product.getVariants(), 
                "Error copying variants for product ID ", 
                product.getId()
            );
            
            // Nếu không thể sao chép, thử lấy từ repository
            if (variantsList.isEmpty() && product.getId() != null) {
                try {
                    variantsList = variantRepository.findByProductId(product.getId());
                } catch (Exception e) {
                    System.err.println("Error retrieving variants from repository for product ID " 
                        + product.getId() + ": " + e.getMessage());
                }
            }
            
            // Xử lý từng variant từ danh sách đã sao chép
            for (ProductVariant variant : variantsList) {
                if (variant == null) {
                    System.err.println("Skipping null variant for product ID: " + product.getId());
                    continue;
                }
                
                try {
                    ProductVariantDTO variantDTO = new ProductVariantDTO();
                    variantDTO.setId(variant.getId());
                    variantDTO.setProductId(product.getId());
                    variantDTO.setColor(variant.getColor() != null ? variant.getColor() : "");
                    variantDTO.setSize(variant.getSize() != null ? variant.getSize() : "");
                    variantDTO.setStockQuantity(variant.getStockQuantity() != null ? variant.getStockQuantity() : 0);
                    variantDTO.setPriceAdjustment(variant.getPriceAdjustment() != null ? variant.getPriceAdjustment() : BigDecimal.ZERO);
                    
                    if (variant.getPriceAdjustment() != null && product.getBasePrice() != null) {
                        variantDTO.setFinalPrice(product.getBasePrice().add(variant.getPriceAdjustment()));
                    } else if (product.getBasePrice() != null) {
                        variantDTO.setFinalPrice(product.getBasePrice());
                    } else {
                        variantDTO.setFinalPrice(BigDecimal.ZERO);
                    }
                    
                    variantDTO.setImage(variant.getImage());
                    
                    if (variant.getStatus() != null) {
                        variantDTO.setStatus(variant.getStatus().name());
                    } else {
                        variantDTO.setStatus("active"); // Default value
                    }

                    // Khởi tạo danh sách rỗng cho variant images
                    List<String> variantImages = new ArrayList<>();
                    
                    // Kiểm tra và lấy ảnh biến thể an toàn
                    if (variant.getId() != null) {
                        try {
                            List<ProductImage> images = productImageRepository.findByVariantId(variant.getId());
                            if (images != null) {
                                for (ProductImage img : images) {
                                    if (img != null && img.getImageURL() != null) {
                                        variantImages.add(img.getImageURL());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error getting variant images for variant ID " + variant.getId() + ": " + e.getMessage());
                            // Continue with empty variant images list
                        }
                    }
                    
                    variantDTO.setImages(variantImages);
                    variantDTOs.add(variantDTO);
                } catch (Exception e) {
                    System.err.println("Error converting variant for product ID " + product.getId() + ": " + e.getMessage());
                    // Continue with next variant
                }
            }
            
            dto.setVariants(variantDTOs);

            // Get product images - khởi tạo danh sách rỗng
            List<String> imageUrls = new ArrayList<>();
            
            // Sao chép an toàn danh sách images
            List<ProductImage> imagesList = safeCollectionCopy(
                product.getImages(),
                "Error copying images for product ID ",
                product.getId()
            );
            
            // Nếu không thể sao chép, thử lấy từ repository
            if (imagesList.isEmpty() && product.getId() != null) {
                try {
                    imagesList = productImageRepository.findByProductId(product.getId());
                } catch (Exception e) {
                    System.err.println("Error retrieving images from repository for product ID " 
                        + product.getId() + ": " + e.getMessage());
                }
            }
            
            // Xử lý từng ảnh từ danh sách đã sao chép
            for (ProductImage img : imagesList) {
                try {
                    if (img != null && img.getImageURL() != null) {
                        imageUrls.add(img.getImageURL());
                    }
                } catch (Exception e) {
                    System.err.println("Error processing image for product ID " + product.getId() + ": " + e.getMessage());
                    // Continue with next image
                }
            }
            
            dto.setImages(imageUrls);

            // Calculate average rating
            try {
                Double avgRating = reviewRepository.calculateAverageRating(product.getId());
                dto.setAverageRating(avgRating != null ? avgRating : 0.0);
            } catch (Exception e) {
                System.err.println("Error calculating average rating for product ID " + product.getId() + ": " + e.getMessage());
                dto.setAverageRating(0.0);
            }

            // Count reviews - Tránh sử dụng collection trực tiếp
            try {
                Long reviewCount = reviewRepository.countByProductId(product.getId());
                dto.setReviewCount(reviewCount != null ? reviewCount : 0L);
            } catch (Exception e) {
                System.err.println("Error counting reviews for product ID " + product.getId() + ": " + e.getMessage());
                dto.setReviewCount(0L);
            }

            return dto;
            
        } catch (Exception e) {
            System.err.println("Error converting product to DTO for product ID " + 
                (product != null ? product.getId() : "null") + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Phương thức helper để sao chép các collection một cách an toàn,
     * tránh ConcurrentModificationException
     */
    private <T> List<T> safeCollectionCopy(Set<T> collection, String errorMessage, Integer entityId) {
        try {
            if (collection == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>(collection);
        } catch (Exception e) {
            System.err.println(errorMessage + entityId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}