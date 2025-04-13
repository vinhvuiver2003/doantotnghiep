package com.example.app.service;

import com.example.app.dto.CategoryDTO;
import com.example.app.dto.PagedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {
    PagedResponse<CategoryDTO> getAllCategories(int page, int size, String sortBy, String sortDir);

    List<CategoryDTO> getAllCategoriesNoPage();

    List<CategoryDTO> getParentCategories();

    List<CategoryDTO> getSubcategories(Integer parentId);

    CategoryDTO getCategoryById(Integer id);

    CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryDTO updateCategory(Integer id, CategoryDTO categoryDTO);

    void deleteCategory(Integer id);

    List<CategoryDTO> getActiveCategories();
    

    CategoryDTO uploadCategoryImage(Integer id, MultipartFile imageFile);
}
