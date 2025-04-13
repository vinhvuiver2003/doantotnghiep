package com.example.app.controller;


import com.example.app.dto.ResponseWrapper;
import com.example.app.dto.CategoryDTO;
import com.example.app.dto.PagedResponse;
import com.example.app.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<PagedResponse<CategoryDTO>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<CategoryDTO> categories = categoryService.getAllCategories(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ResponseWrapper.success("Categories retrieved successfully", categories));
    }


    @GetMapping("/all")
    public ResponseEntity<ResponseWrapper<List<CategoryDTO>>> getAllCategoriesNoPage() {
        List<CategoryDTO> categories = categoryService.getAllCategoriesNoPage();
        return ResponseEntity.ok(ResponseWrapper.success("All categories retrieved successfully", categories));
    }


    @GetMapping("/parent")
    public ResponseEntity<ResponseWrapper<List<CategoryDTO>>> getParentCategories() {
        List<CategoryDTO> parentCategories = categoryService.getParentCategories();
        return ResponseEntity.ok(ResponseWrapper.success("Parent categories retrieved successfully", parentCategories));
    }


    @GetMapping("/{id}/subcategories")
    public ResponseEntity<ResponseWrapper<List<CategoryDTO>>> getSubcategories(@PathVariable Integer id) {
        List<CategoryDTO> subcategories = categoryService.getSubcategories(id);
        return ResponseEntity.ok(ResponseWrapper.success("Subcategories retrieved successfully", subcategories));
    }


    @GetMapping("/active")
    public ResponseEntity<ResponseWrapper<List<CategoryDTO>>> getActiveCategories() {
        List<CategoryDTO> activeCategories = categoryService.getActiveCategories();
        return ResponseEntity.ok(ResponseWrapper.success("Active categories retrieved successfully", activeCategories));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<CategoryDTO>> getCategoryById(@PathVariable Integer id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ResponseWrapper.success("Category retrieved successfully", category));
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(
                ResponseWrapper.success("Category created successfully", createdCategory),
                HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<CategoryDTO>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryDTO categoryDTO) {

        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(ResponseWrapper.success("Category updated successfully", updatedCategory));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<?>> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ResponseWrapper.success("Category deleted successfully"));
    }
    

    @PostMapping(value = "/upload-image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<CategoryDTO>> uploadCategoryImage(
            @PathVariable Integer id,
            @RequestParam("image") MultipartFile imageFile) {
        
        CategoryDTO updatedCategory = categoryService.uploadCategoryImage(id, imageFile);
        return ResponseEntity.ok(ResponseWrapper.success("Category image uploaded successfully", updatedCategory));
    }
}