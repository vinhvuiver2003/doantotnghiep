package com.example.app.service.impl;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.ProductDTO;
import com.example.app.dto.ProductImageDTO;
import com.example.app.dto.ProductVariantDTO;
import com.example.app.dto.RelatedProductDTO;
import com.example.app.dto.ReviewDTO;
import com.example.app.entity.*;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.*;
import com.example.app.service.ProductService;
import com.example.app.service.RelatedProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
    private final RelatedProductService relatedProductService;

    @Autowired
    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository productImageRepository,
            ReviewRepository reviewRepository,
            RelatedProductService relatedProductService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.variantRepository = variantRepository;
        this.productImageRepository = productImageRepository;
        this.reviewRepository = reviewRepository;
        this.relatedProductService = relatedProductService;
    }

    @Override
    public PagedResponse<ProductDTO> getAllProducts(int page, int size, String sortBy, String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Product> products;
            if (sortBy.equals("id") || sortBy.equals("name") || sortBy.equals("basePrice") || sortBy.equals("averageRating")) {
                List<Integer> productIds;
                
                productIds = productRepository.findAll(pageable)
                    .stream().map(Product::getId).collect(Collectors.toList());
                
                if (productIds.isEmpty()) {
                    return new PagedResponse<>(
                        new ArrayList<>(), 
                        page, 
                        size, 
                        0, 
                        0, 
                        true
                    );
                }
                
                List<Product> productList = productRepository.findByIdInWithVariantsAndImages(productIds);
                
                productList.sort((p1, p2) -> {
                    if (sortBy.equals("id")) {
                        return sortDir.equalsIgnoreCase("desc") ? 
                            p2.getId().compareTo(p1.getId()) : 
                            p1.getId().compareTo(p2.getId());
                    } else if (sortBy.equals("name")) {
                        return sortDir.equalsIgnoreCase("desc") ? 
                            p2.getName().compareTo(p1.getName()) : 
                            p1.getName().compareTo(p2.getName());
                    } else if (sortBy.equals("averageRating")) {
                        Double rating1 = reviewRepository.findAverageRatingByProductId(p1.getId());
                        Double rating2 = reviewRepository.findAverageRatingByProductId(p2.getId());
                        
                        rating1 = rating1 != null ? rating1 : 0.0;
                        rating2 = rating2 != null ? rating2 : 0.0;
                        
                        return sortDir.equalsIgnoreCase("desc") ? 
                            Double.compare(rating2, rating1) : 
                            Double.compare(rating1, rating2);
                    } else {
                        return sortDir.equalsIgnoreCase("desc") ? 
                            p2.getBasePrice().compareTo(p1.getBasePrice()) : 
                            p1.getBasePrice().compareTo(p2.getBasePrice());
                    }
                });
                
                final int start = (int)pageable.getOffset();
                final int end = Math.min((start + pageable.getPageSize()), productList.size());
                
                List<ProductDTO> content = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    Product product = productList.get(i);
                    try {
                        ProductDTO dto = convertToDTOSafe(product);
                        if (dto != null) {
                            content.add(dto);
                        } else {
                            System.err.println("Skipping null product DTO for product ID: " + 
                                (product != null ? product.getId() : "unknown"));
                        }
                    } catch (Exception e) {
                        System.err.println("Error converting product with ID " + 
                            (product != null ? product.getId() : "unknown") + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                return new PagedResponse<>(
                    content,
                    page,
                    size,
                    productRepository.count(),
                    (int) Math.ceil((double) productRepository.count() / size),
                    (page + 1) * size >= productRepository.count()
                );
            } else {
                products = productRepository.findAll(pageable);
                
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
            }
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
    public PagedResponse<ProductDTO> getProductsByCategory(Integer categoryId, int page, int size, String sortBy, String sortDir) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        
        List<Integer> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);
        
        if (category.getParent() == null) {
            List<Category> childCategories = categoryRepository.findByParentId(categoryId);
            categoryIds.addAll(childCategories.stream()
                .map(Category::getId)
                .collect(Collectors.toList()));
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Product> products = productRepository.findByCategoryIdIn(categoryIds, pageable);
        
        return new PagedResponse<>(
            products.getContent().stream()
                .map(this::convertToDTOSafe)
                .collect(Collectors.toList()),
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages(),
            products.isLast()
        );
    }

    @Override
    public PagedResponse<ProductDTO> getProductsByBrand(Integer brandId, int page, int size, String sortBy, String sortDir) {
        if (!brandRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("Brand not found with id: " + brandId);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
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
    public PagedResponse<ProductDTO> searchProducts(String keyword, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
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
    public PagedResponse<ProductDTO> filterProductsByPrice(BigDecimal minPrice, BigDecimal maxPrice, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
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
        List<Product> topRatedProducts = productRepository.findTopRatedProducts(limit);
        return topRatedProducts.stream()
                .map(this::convertToDTOSafe)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getBestSellingProducts(int limit) {
        List<Product> bestSellingProducts = productRepository.findBestSellingProducts(limit);
        return bestSellingProducts.stream()
                .map(this::convertToDTOSafe)
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

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));

        Brand brand = brandRepository.findById(productDTO.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + productDTO.getBrandId()));

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice() != null ? productDTO.getBasePrice() : BigDecimal.ZERO);
        product.setCategory(category);
        product.setBrand(brand);
        
        if (productDTO.getProductType() != null) {
            product.setProductType(Product.ProductType.valueOf(productDTO.getProductType()));
        } else {
            product.setProductType(Product.ProductType.clothing);
        }

        if (productDTO.getStatus() != null) {
            product.setStatus(Product.ProductStatus.valueOf(productDTO.getStatus()));
        }

        Product savedProduct = productRepository.save(product);

        ProductVariant defaultVariant = null;
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
                
                if (variantDTO.getSizeType() != null) {
                    variant.setSizeType(ProductVariant.SizeType.valueOf(variantDTO.getSizeType()));
                } else {
                    if (product.getProductType() == Product.ProductType.footwear) {
                        variant.setSizeType(ProductVariant.SizeType.shoe_size);
                    } else {
                        variant.setSizeType(ProductVariant.SizeType.clothing_size);
                    }
                }
                
                variant.setStockQuantity(variantDTO.getStockQuantity() != null ? variantDTO.getStockQuantity() : 0);
                variant.setPriceAdjustment(variantDTO.getPriceAdjustment() != null ? variantDTO.getPriceAdjustment() : BigDecimal.ZERO);
                variant.setSku(variantDTO.getSku());

                if (variantDTO.getStatus() != null) {
                    variant.setStatus(ProductVariant.VariantStatus.valueOf(variantDTO.getStatus()));
                }

                variant = variantRepository.save(variant);
                
                if ((variantDTO.getIsPrimary() != null && variantDTO.getIsPrimary()) || defaultVariant == null) {
                    defaultVariant = variant;
                }
                
                if (variantDTO.getImages() != null && !variantDTO.getImages().isEmpty()) {
                    for (ProductImageDTO imageDTO : variantDTO.getImages()) {
                        ProductImage image = new ProductImage();
                        image.setProduct(savedProduct);
                        image.setVariant(variant);
                        image.setImageURL(imageDTO.getImageURL());
                        image.setIsPrimary(imageDTO.getIsPrimary() != null ? imageDTO.getIsPrimary() : false);
                        image.setSortOrder(imageDTO.getSortOrder() != null ? imageDTO.getSortOrder() : 0);
                        image.setAltText(imageDTO.getAltText());
                        
                        productImageRepository.save(image);
                    }
                }
            }
        }
        
        if (defaultVariant != null) {
            savedProduct.setDefaultVariant(defaultVariant);
            savedProduct = productRepository.save(savedProduct);
        }

        if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
            int sortOrder = 0;
            for (String imageUrl : productDTO.getImages()) {
                ProductImage image = new ProductImage();
                image.setProduct(savedProduct);
                image.setVariant(null); // Ảnh cấp sản phẩm, không thuộc variant nào
                image.setImageURL(imageUrl);
                image.setIsPrimary(sortOrder == 0); // Ảnh đầu tiên là ảnh chính
                image.setSortOrder(sortOrder++);

                productImageRepository.save(image);
            }
        }

        return convertToDTOSafe(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Integer id, ProductDTO productDTO) {
        if (productDTO.getBasePrice() != null && productDTO.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá sản phẩm không được âm");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setBasePrice(productDTO.getBasePrice() != null ? productDTO.getBasePrice() : product.getBasePrice());
        
        if (productDTO.getProductType() != null) {
            product.setProductType(Product.ProductType.valueOf(productDTO.getProductType()));
        }

        if (productDTO.getStatus() != null) {
            product.setStatus(Product.ProductStatus.valueOf(productDTO.getStatus()));
        }

        if (productDTO.getCategoryId() != null &&
                (product.getCategory() == null || !product.getCategory().getId().equals(productDTO.getCategoryId()))) {

            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }

        if (productDTO.getBrandId() != null &&
                (product.getBrand() == null || !product.getBrand().getId().equals(productDTO.getBrandId()))) {

            Brand brand = brandRepository.findById(productDTO.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + productDTO.getBrandId()));
            product.setBrand(brand);
        }

        Product updatedProduct = productRepository.save(product);

        ProductVariant defaultVariant = updatedProduct.getDefaultVariant();
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
                    
                    if (variantDTO.getSizeType() != null) {
                        variant.setSizeType(ProductVariant.SizeType.valueOf(variantDTO.getSizeType()));
                    }
                    
                    variant.setStockQuantity(variantDTO.getStockQuantity() != null ? variantDTO.getStockQuantity() : variant.getStockQuantity());
                    variant.setPriceAdjustment(variantDTO.getPriceAdjustment() != null ? variantDTO.getPriceAdjustment() : variant.getPriceAdjustment());
                    variant.setSku(variantDTO.getSku());
                } else {
                    variant = new ProductVariant();
                    variant.setProduct(updatedProduct);
                    variant.setColor(variantDTO.getColor());
                    variant.setSize(variantDTO.getSize());
                    
                    if (variantDTO.getSizeType() != null) {
                        variant.setSizeType(ProductVariant.SizeType.valueOf(variantDTO.getSizeType()));
                    } else {
                        if (product.getProductType() == Product.ProductType.footwear) {
                            variant.setSizeType(ProductVariant.SizeType.shoe_size);
                        } else {
                            variant.setSizeType(ProductVariant.SizeType.clothing_size);
                        }
                    }
                    
                    variant.setStockQuantity(variantDTO.getStockQuantity() != null ? variantDTO.getStockQuantity() : 0);
                    variant.setPriceAdjustment(variantDTO.getPriceAdjustment() != null ? variantDTO.getPriceAdjustment() : BigDecimal.ZERO);
                    variant.setSku(variantDTO.getSku());
                }

                if (variantDTO.getStatus() != null) {
                    variant.setStatus(ProductVariant.VariantStatus.valueOf(variantDTO.getStatus()));
                }

                variant = variantRepository.save(variant);
                
                if ((variantDTO.getIsPrimary() != null && variantDTO.getIsPrimary()) ||
                    (defaultVariant == null && updatedProduct.getDefaultVariant() == null)) {
                    defaultVariant = variant;
                }
                
                if (variantDTO.getImages() != null) {
                    if (variant.getId() != null) {
                        productImageRepository.deleteByVariantId(variant.getId());
                    }
                    
                    for (ProductImageDTO imageDTO : variantDTO.getImages()) {
                        ProductImage image = new ProductImage();
                        image.setProduct(updatedProduct);
                        image.setVariant(variant);
                        image.setImageURL(imageDTO.getImageURL());
                        image.setIsPrimary(imageDTO.getIsPrimary() != null ? imageDTO.getIsPrimary() : false);
                        image.setSortOrder(imageDTO.getSortOrder() != null ? imageDTO.getSortOrder() : 0);
                        image.setAltText(imageDTO.getAltText());
                        
                        productImageRepository.save(image);
                    }
                }
            }
        }
        
        if (defaultVariant != null && (updatedProduct.getDefaultVariant() == null ||
                !updatedProduct.getDefaultVariant().getId().equals(defaultVariant.getId()))) {
            updatedProduct.setDefaultVariant(defaultVariant);
            updatedProduct = productRepository.save(updatedProduct);
        }

        if (productDTO.getImages() != null) {
            productImageRepository.deleteByProductIdAndVariantIsNull(id);

            if (!productDTO.getImages().isEmpty()) {
                int sortOrder = 0;
                for (String imageUrl : productDTO.getImages()) {
                    ProductImage image = new ProductImage();
                    image.setProduct(updatedProduct);
                    image.setVariant(null); // Ảnh cấp sản phẩm, không thuộc variant nào
                    image.setImageURL(imageUrl);
                    image.setIsPrimary(sortOrder == 0); // Ảnh đầu tiên là ảnh chính
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
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }

        productImageRepository.deleteByProductId(id);

        productRepository.deleteById(id);
    }

    @Override
    public Long countProducts() {
        return productRepository.count();
    }

    @Override
    public List<ProductDTO> getRandomProducts(int limit) {
        List<Integer> allProductIds = productRepository.findAllProductIds();
        
        if (allProductIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        Collections.shuffle(allProductIds);
        
        int size = Math.min(limit, allProductIds.size());
        List<Integer> randomIds = allProductIds.subList(0, size);
        
        List<Product> randomProducts = productRepository.findByIdInWithVariantsAndImages(randomIds);
        
        return randomProducts.stream()
            .map(this::convertToDTOSafe)
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<ProductDTO> getProductsByCategoryAndBrand(Integer categoryId, Integer brandId, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Product> products = productRepository.findByCategoryIdAndBrandId(categoryId, brandId, pageable);
        return new PagedResponse<>(
            products.getContent().stream()
                .map(this::convertToDTOSafe)
                .collect(Collectors.toList()),
            products.getNumber(),
            products.getSize(),
            products.getTotalElements(),
            products.getTotalPages(),
            products.isLast()
        );
    }

    private ProductDTO convertToDTOSafe(Product product) {
        try {
            if (product == null) {
                return null;
            }

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
            
            if (product.getProductType() != null) {
                dto.setProductType(product.getProductType().name());
            }
            
            try {
                dto.setTotalStockQuantity(product.getTotalStockQuantity());
            } catch (Exception e) {
                System.err.println("Error calculating total stock for product ID " + product.getId() + ": " + e.getMessage());
                int totalStock = 0;
                List<ProductVariant> safeVariants = safeCollectionCopy(product.getVariants(), 
                    "Error copying variants for product ID ", product.getId());
                for (ProductVariant variant : safeVariants) {
                    if (variant != null && variant.getStockQuantity() != null) {
                        totalStock += variant.getStockQuantity();
                    }
                }
                dto.setTotalStockQuantity(totalStock);
            }
            
            if (product.getDefaultVariant() != null) {
                dto.setDefaultVariantId(product.getDefaultVariant().getId());
            }
            
            if (product.getStatus() != null) {
                dto.setStatus(product.getStatus().name());
            }
            
            dto.setCreatedAt(product.getCreatedAt());
            dto.setUpdatedAt(product.getUpdatedAt());
            
            List<ProductVariant> safeVariants = safeCollectionCopy(product.getVariants(),
                "Error copying variants for product ID ", product.getId());
            
            if (!safeVariants.isEmpty()) {
                List<ProductVariantDTO> variantDTOs = new ArrayList<>();
                for (ProductVariant variant : safeVariants) {
                    try {
                        ProductVariantDTO variantDTO = convertVariantToDTOSafe(variant);
                        if (variantDTO != null) {
                            variantDTOs.add(variantDTO);
                        }
                    } catch (Exception e) {
                        System.err.println("Error converting variant for product ID " + product.getId() + ": " + e.getMessage());
                    }
                }
                dto.setVariants(variantDTOs);
            } else {
                dto.setVariants(new ArrayList<>());
            }
            
            List<ProductImage> safeImages = safeCollectionCopy(product.getImages(),
                "Error copying images for product ID ", product.getId());
            
            if (!safeImages.isEmpty()) {
                List<String> imageUrls = new ArrayList<>();
                for (ProductImage img : safeImages) {
                    if (img != null && img.getVariant() == null && img.getImageURL() != null) {
                        imageUrls.add(img.getImageURL());
                    }
                }
                dto.setImages(imageUrls);
            } else {
                dto.setImages(new ArrayList<>());
            }
            
            try {
                Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId());
                dto.setAverageRating(avgRating != null ? avgRating : 0.0);
                
                Long reviewCount = reviewRepository.countByProductId(product.getId());
                dto.setReviewCount(reviewCount != null ? reviewCount : 0L);
                
                Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
                Page<Review> reviews = reviewRepository.findByProductId(product.getId(), pageable);
                
                List<ReviewDTO> reviewDTOs = reviews.getContent().stream()
                    .map(review -> {
                        ReviewDTO reviewDTO = new ReviewDTO();
                        reviewDTO.setId(review.getId());
                        reviewDTO.setProductId(review.getProduct().getId());
                        reviewDTO.setProductName(review.getProduct().getName());
                        reviewDTO.setUserId(review.getUser().getId());
                        reviewDTO.setUsername(review.getUser().getUsername());
                        reviewDTO.setRating(review.getRating());
                        reviewDTO.setTitle(review.getTitle());
                        reviewDTO.setContent(review.getContent());
                        reviewDTO.setComment(review.getComment());
                        reviewDTO.setCreatedAt(review.getCreatedAt());
                        reviewDTO.setUpdatedAt(review.getUpdatedAt());
                        return reviewDTO;
                    })
                    .collect(Collectors.toList());
                
                dto.setReviews(reviewDTOs);
            } catch (Exception e) {
                System.err.println("Error getting review stats for product ID " + product.getId() + ": " + e.getMessage());
                dto.setAverageRating(0.0);
                dto.setReviewCount(0L);
                dto.setReviews(new ArrayList<>());
            }
            
            try {
                List<RelatedProductDTO> relatedProducts = relatedProductService.getRelatedProducts(product.getId());
                dto.setRelatedProducts(relatedProducts);
            } catch (Exception e) {
                System.err.println("Error getting related products for product ID " + product.getId() + ": " + e.getMessage());
                dto.setRelatedProducts(new ArrayList<>());
            }
            
            return dto;
        } catch (Exception e) {
            System.err.println("Error converting product to DTO: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private ProductVariantDTO convertVariantToDTOSafe(ProductVariant variant) {
        try {
            if (variant == null) {
                return null;
            }

            ProductVariantDTO dto = new ProductVariantDTO();
            dto.setId(variant.getId());
            
            if (variant.getProduct() != null) {
                dto.setProductId(variant.getProduct().getId());
            }
            
            dto.setColor(variant.getColor());
            dto.setSize(variant.getSize());
            
            if (variant.getSizeType() != null) {
                dto.setSizeType(variant.getSizeType().name());
            }
            
            dto.setStockQuantity(variant.getStockQuantity());
            dto.setPriceAdjustment(variant.getPriceAdjustment());
            
            try {
                dto.setFinalPrice(variant.getFinalPrice());
            } catch (Exception e) {
                System.err.println("Error calculating final price for variant ID " + variant.getId() + ": " + e.getMessage());
                if (variant.getProduct() != null && variant.getProduct().getBasePrice() != null) {
                    dto.setFinalPrice(variant.getProduct().getBasePrice().add(variant.getPriceAdjustment()));
                } else {
                    dto.setFinalPrice(variant.getPriceAdjustment());
                }
            }
            
            dto.setSku(variant.getSku());
            
            if (variant.getStatus() != null) {
                dto.setStatus(variant.getStatus().name());
            }
            
            try {
                Product product = variant.getProduct();
                if (product != null && product.getDefaultVariant() != null) {
                    dto.setIsPrimary(product.getDefaultVariant().getId().equals(variant.getId()));
                } else {
                    dto.setIsPrimary(false);
                }
            } catch (Exception e) {
                System.err.println("Error checking if variant is primary for variant ID " + variant.getId() + ": " + e.getMessage());
                dto.setIsPrimary(false);
            }
            
            List<ProductImage> safeImages = safeCollectionCopy(variant.getImages(),
                "Error copying images for variant ID ", variant.getId());
            
            if (!safeImages.isEmpty()) {
                List<ProductImageDTO> imageDTOs = new ArrayList<>();
                for (ProductImage image : safeImages) {
                    try {
                        ProductImageDTO imageDTO = convertImageToDTOSafe(image);
                        if (imageDTO != null) {
                            imageDTOs.add(imageDTO);
                        }
                    } catch (Exception e) {
                        System.err.println("Error converting image for variant ID " + variant.getId() + ": " + e.getMessage());
                    }
                }
                dto.setImages(imageDTOs);
            } else {
                dto.setImages(new ArrayList<>());
            }
            
            return dto;
        } catch (Exception e) {
            System.err.println("Error converting variant to DTO: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private ProductImageDTO convertImageToDTOSafe(ProductImage image) {
        try {
            if (image == null) {
                return null;
            }

            ProductImageDTO dto = new ProductImageDTO();
            dto.setId(image.getId());
            
            if (image.getProduct() != null) {
                dto.setProductId(image.getProduct().getId());
            }
            
            if (image.getVariant() != null) {
                dto.setVariantId(image.getVariant().getId());
            }
            
            dto.setImageURL(image.getImageURL());
            dto.setIsPrimary(image.getIsPrimary());
            dto.setSortOrder(image.getSortOrder());
            dto.setAltText(image.getAltText());
            dto.setCreatedAt(image.getCreatedAt());
            
            return dto;
        } catch (Exception e) {
            System.err.println("Error converting image to DTO: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


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