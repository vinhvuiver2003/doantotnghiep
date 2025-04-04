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
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            PagedResponse<ProductDTO> products = productService.getAllProducts(page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving products: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin chi tiết một sản phẩm theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Integer id) {
        try {
            ProductDTO product = productService.getProductById(id);
            return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving product: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách sản phẩm theo danh mục
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            PagedResponse<ProductDTO> products = productService.getProductsByCategory(categoryId, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success("Products by category retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving products by category: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách sản phẩm theo thương hiệu
     */
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<?> getProductsByBrand(
            @PathVariable Integer brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            PagedResponse<ProductDTO> products = productService.getProductsByBrand(brandId, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success("Products by brand retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving products by brand: " + e.getMessage()));
        }
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            PagedResponse<ProductDTO> products = productService.searchProducts(keyword, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success("Search results", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error searching products: " + e.getMessage()));
        }
    }

    /**
     * Lọc sản phẩm theo khoảng giá
     */
    @GetMapping("/filter/price")
    public ResponseEntity<?> filterProductsByPrice(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            PagedResponse<ProductDTO> products = productService.filterProductsByPrice(minPrice, maxPrice, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success("Price filtered results", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error filtering products by price: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách sản phẩm mới nhất
     */
    @GetMapping("/new-arrivals")
    public ResponseEntity<?> getNewArrivals(
            @RequestParam(defaultValue = "8") int limit) {

        try {
            List<ProductDTO> products = productService.getNewArrivals(limit);
            return ResponseEntity.ok(ApiResponse.success("New arrivals retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving new arrivals: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách sản phẩm được đánh giá cao nhất
     */
    @GetMapping("/top-rated")
    public ResponseEntity<?> getTopRatedProducts(
            @RequestParam(defaultValue = "8") int limit) {

        try {
            List<ProductDTO> products = productService.getTopRatedProducts(limit);
            return ResponseEntity.ok(ApiResponse.success("Top rated products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving top rated products: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách sản phẩm có tồn kho thấp (chỉ ADMIN)
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {

        try {
            List<ProductDTO> products = productService.getLowStockProducts(threshold);
            return ResponseEntity.ok(ApiResponse.success("Low stock products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving low stock products: " + e.getMessage()));
        }
    }

    /**
     * Tạo mới một sản phẩm (chỉ ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO createdProduct = productService.createProduct(productDTO);
            return new ResponseEntity<>(
                    ApiResponse.success("Product created successfully", createdProduct),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating product: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật thông tin một sản phẩm (chỉ ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody ProductDTO productDTO) {

        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating product: " + e.getMessage()));
        }
    }

    /**
     * Xóa một sản phẩm (chỉ ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error deleting product: " + e.getMessage()));
        }
    }

    @GetMapping("/random")
    public ResponseEntity<List<ProductDTO>> getRandomProducts(
        @RequestParam(defaultValue = "4") int limit
    ) {
        List<ProductDTO> randomProducts = productService.getRandomProducts(limit);
        return ResponseEntity.ok(randomProducts);
    }
}