package com.example.app.controller;
import com.example.app.dto.ApiResponse;
import com.example.app.dto.BrandDTO;
import com.example.app.dto.PagedResponse;
import com.example.app.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;

    @Autowired
    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    /**
     * Lấy danh sách tất cả thương hiệu với phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BrandDTO>>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<BrandDTO> brands = brandService.getAllBrands(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Brands retrieved successfully", brands));
    }

    /**
     * Lấy danh sách tất cả thương hiệu không phân trang (cho dropdown)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BrandDTO>>> getAllBrandsNoPage() {
        List<BrandDTO> brands = brandService.getAllBrandsNoPage();
        return ResponseEntity.ok(ApiResponse.success("All brands retrieved successfully", brands));
    }

    /**
     * Lấy thông tin chi tiết một thương hiệu theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandDTO>> getBrandById(@PathVariable Integer id) {
        BrandDTO brand = brandService.getBrandById(id);
        return ResponseEntity.ok(ApiResponse.success("Brand retrieved successfully", brand));
    }

    /**
     * Tạo mới một thương hiệu (Chỉ ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BrandDTO>> createBrand(@Valid @RequestBody BrandDTO brandDTO) {
        BrandDTO createdBrand = brandService.createBrand(brandDTO);
        return new ResponseEntity<>(
                ApiResponse.success("Brand created successfully", createdBrand),
                HttpStatus.CREATED);
    }

    /**
     * Cập nhật thông tin một thương hiệu (Chỉ ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BrandDTO>> updateBrand(
            @PathVariable Integer id,
            @Valid @RequestBody BrandDTO brandDTO) {

        BrandDTO updatedBrand = brandService.updateBrand(id, brandDTO);
        return ResponseEntity.ok(ApiResponse.success("Brand updated successfully", updatedBrand));
    }

    /**
     * Xóa một thương hiệu (Chỉ ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteBrand(@PathVariable Integer id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.success("Brand deleted successfully"));
    }
}