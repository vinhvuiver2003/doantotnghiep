package com.example.app.controller;
import com.example.app.dto.ResponseWrapper;
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


    @GetMapping
    public ResponseEntity<ResponseWrapper<PagedResponse<BrandDTO>>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<BrandDTO> brands = brandService.getAllBrands(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ResponseWrapper.success("Brands retrieved successfully", brands));
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseWrapper<List<BrandDTO>>> getAllBrandsNoPage() {
        List<BrandDTO> brands = brandService.getAllBrandsNoPage();
        return ResponseEntity.ok(ResponseWrapper.success("All brands retrieved successfully", brands));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<BrandDTO>> getBrandById(@PathVariable Integer id) {
        BrandDTO brand = brandService.getBrandById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Brand retrieved successfully", brand));
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<BrandDTO>> createBrand(@Valid @RequestBody BrandDTO brandDTO) {
        BrandDTO createdBrand = brandService.createBrand(brandDTO);
        return new ResponseEntity<>(
                ResponseWrapper.success("Brand created successfully", createdBrand),
                HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<BrandDTO>> updateBrand(
            @PathVariable Integer id,
            @Valid @RequestBody BrandDTO brandDTO) {

        BrandDTO updatedBrand = brandService.updateBrand(id, brandDTO);
        return ResponseEntity.ok(ResponseWrapper.success("Brand updated successfully", updatedBrand));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<?>> deleteBrand(@PathVariable Integer id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ResponseWrapper.success("Brand deleted successfully"));
    }
}