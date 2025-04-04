package com.example.app.service;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.ProductDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    PagedResponse<ProductDTO> getAllProducts(int page, int size, String sortBy, String sortDir);

    ProductDTO getProductById(Integer id);

    PagedResponse<ProductDTO> getProductsByCategory(Integer categoryId, int page, int size, String sortBy, String sortDir);

    PagedResponse<ProductDTO> getProductsByBrand(Integer brandId, int page, int size, String sortBy, String sortDir);

    PagedResponse<ProductDTO> searchProducts(String keyword, int page, int size, String sortBy, String sortDir);

    PagedResponse<ProductDTO> filterProductsByPrice(BigDecimal minPrice, BigDecimal maxPrice, int page, int size, String sortBy, String sortDir);

    List<ProductDTO> getNewArrivals(int limit);

    List<ProductDTO> getTopRatedProducts(int limit);

    List<ProductDTO> getLowStockProducts(int threshold);

    List<ProductDTO> getRandomProducts(int limit);

    ProductDTO createProduct(ProductDTO productDTO);

    ProductDTO updateProduct(Integer id, ProductDTO productDTO);

    void deleteProduct(Integer id);

    Long countProducts();
}