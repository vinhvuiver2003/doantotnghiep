package com.example.app.controller;
import com.example.app.dto.ApiResponse;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.ProductDTO;
import com.example.app.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Lấy danh sách tất cả sản phẩm với phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PagedResponse<ProductDTO> products = productService.getAllProducts(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    /**
     * Lấy thông tin chi tiết một sản phẩm theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Integer id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    /**
     * Lấy danh sách sản phẩm theo danh mục
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO>>> getProductsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        PagedResponse<ProductDTO> products = productService.getProductsByCategory(categoryId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Products by category retrieved successfully", products));
    }

    /**
     * Lấy danh sách sản phẩm theo thương hiệu
     */
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO>>> getProductsByBrand(
            @PathVariable Integer brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        PagedResponse<ProductDTO> products = productService.getProductsByBrand(brandId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Products by brand retrieved successfully", products));
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        PagedResponse<ProductDTO> products = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("Search results", products));
    }

    /**
     * Lọc sản phẩm theo khoảng giá
     */
    @GetMapping("/filter/price")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDTO>>> filterProductsByPrice(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        PagedResponse<ProductDTO> products = productService.filterProductsByPrice(minPrice, maxPrice, page, size);
        return ResponseEntity.ok(ApiResponse.success("Price filtered results", products));
    }

    /**
     * Lấy danh sách sản phẩm mới nhất
     */
    @GetMapping("/new-arrivals")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getNewArrivals(
            @RequestParam(defaultValue = "8") int limit) {

        List<ProductDTO> products = productService.getNewArrivals(limit);
        return ResponseEntity.ok(ApiResponse.success("New arrivals retrieved successfully", products));
    }

    /**
     * Lấy danh sách sản phẩm được đánh giá cao nhất
     */
    @GetMapping("/top-rated")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getTopRatedProducts(
            @RequestParam(defaultValue = "8") int limit) {

        List<ProductDTO> products = productService.getTopRatedProducts(limit);
        return ResponseEntity.ok(ApiResponse.success("Top rated products retrieved successfully", products));
    }

    /**
     * Lấy danh sách sản phẩm có tồn kho thấp (chỉ ADMIN)
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {

        List<ProductDTO> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(ApiResponse.success("Low stock products retrieved successfully", products));
    }

    /**
     * Tạo mới một sản phẩm (chỉ ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(
                ApiResponse.success("Product created successfully", createdProduct),
                HttpStatus.CREATED);
    }

    /**
     * Cập nhật thông tin một sản phẩm (chỉ ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody ProductDTO productDTO) {

        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updatedProduct));
    }

    /**
     * Xóa một sản phẩm (chỉ ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }
}