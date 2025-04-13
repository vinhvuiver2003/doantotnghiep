package com.example.app.controller;
import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.PagedResponse;
import com.example.app.dto.ProductDTO;
import com.example.app.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Sản phẩm", description = "API quản lý sản phẩm")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @Operation(
            summary = "Lấy danh sách sản phẩm có phân trang",
            description = "Trả về danh sách sản phẩm phân trang theo các tiêu chí sắp xếp"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách sản phẩm thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "500", description = "Lỗi server khi lấy danh sách sản phẩm"),
    })
    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số sản phẩm trên mỗi trang") @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "Sắp xếp theo trường") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Chiều sắp xếp (asc hoặc desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            PagedResponse<ProductDTO> products = productService.getAllProducts(page, size, sortBy, sortDir);
            return ResponseEntity.ok(ResponseWrapper.success("Products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error retrieving products: " + e.getMessage()));
        }
    }


    @Operation(
            summary = "Lấy thông tin sản phẩm theo ID",
            description = "Trả về thông tin chi tiết của sản phẩm theo ID cung cấp"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin sản phẩm thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm với ID cung cấp"),
            @ApiResponse(responseCode = "500", description = "Lỗi server khi lấy thông tin sản phẩm")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(
            @Parameter(description = "ID của sản phẩm") @PathVariable Integer id) {
        try {
            ProductDTO product = productService.getProductById(id);
            return ResponseEntity.ok(ResponseWrapper.success("Product retrieved successfully", product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error retrieving product: " + e.getMessage()));
        }
    }


    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            PagedResponse<ProductDTO> products = productService.getProductsByCategory(categoryId, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ResponseWrapper.success("Products by category retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error retrieving products by category: " + e.getMessage()));
        }
    }


    @GetMapping("/brand/{brandId}")
    public ResponseEntity<?> getProductsByBrand(
            @PathVariable Integer brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            PagedResponse<ProductDTO> products = productService.getProductsByBrand(brandId, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ResponseWrapper.success("Products by brand retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error retrieving products by brand: " + e.getMessage()));
        }
    }


    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            PagedResponse<ProductDTO> products = productService.searchProducts(keyword, page, size, sortBy, sortDir);
            return ResponseEntity.ok(ResponseWrapper.success("Search results", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error searching products: " + e.getMessage()));
        }
    }


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
            return ResponseEntity.ok(ResponseWrapper.success("Price filtered results", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error filtering products by price: " + e.getMessage()));
        }
    }


    @GetMapping("/new-arrivals")
    public ResponseEntity<?> getNewArrivals(
            @RequestParam(defaultValue = "8") int limit) {

        try {
            List<ProductDTO> products = productService.getNewArrivals(limit);
            return ResponseEntity.ok(ResponseWrapper.success("New arrivals retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error retrieving new arrivals: " + e.getMessage()));
        }
    }


    @GetMapping("/top-rated")
    public ResponseEntity<?> getTopRatedProducts(
            @RequestParam(defaultValue = "8") int limit) {

        try {
            List<ProductDTO> products = productService.getTopRatedProducts(limit);
            return ResponseEntity.ok(ResponseWrapper.success("Top rated products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error retrieving top rated products: " + e.getMessage()));
        }
    }


    @GetMapping("/best-selling")
    public ResponseEntity<?> getBestSellingProducts(
            @RequestParam(defaultValue = "8") int limit) {

        try {
            List<ProductDTO> products = productService.getBestSellingProducts(limit);
            return ResponseEntity.ok(ResponseWrapper.success("Best selling products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error retrieving best selling products: " + e.getMessage()));
        }
    }


    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {

        try {
            List<ProductDTO> products = productService.getLowStockProducts(threshold);
            return ResponseEntity.ok(ResponseWrapper.success("Low stock products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error retrieving low stock products: " + e.getMessage()));
        }
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO createdProduct = productService.createProduct(productDTO);
            return new ResponseEntity<>(
                    ResponseWrapper.success("Product created successfully", createdProduct),
                    HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error creating product: " + e.getMessage()));
        }
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody ProductDTO productDTO) {

        try {
            ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(ResponseWrapper.success("Product updated successfully", updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error updating product: " + e.getMessage()));
        }
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ResponseWrapper.success("Product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapper.error("Error deleting product: " + e.getMessage()));
        }
    }

    @GetMapping("/random")
    public ResponseEntity<List<ProductDTO>> getRandomProducts(
        @RequestParam(defaultValue = "4") int limit
    ) {
        List<ProductDTO> randomProducts = productService.getRandomProducts(limit);
        return ResponseEntity.ok(randomProducts);
    }

    @GetMapping("/category/{categoryId}/brand/{brandId}")
    public ResponseEntity<PagedResponse<ProductDTO>> getProductsByCategoryAndBrand(
        @PathVariable Integer categoryId,
        @PathVariable Integer brandId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir) {
        PagedResponse<ProductDTO> response = productService.getProductsByCategoryAndBrand(
            categoryId, brandId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }
}