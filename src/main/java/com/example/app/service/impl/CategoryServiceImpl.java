package com.example.app.service.impl;
import com.example.app.dto.CategoryDTO;
import com.example.app.dto.PagedResponse;
import com.example.app.entity.Category;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.repository.CategoryRepository;
import com.example.app.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public PagedResponse<CategoryDTO> getAllCategories(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Category> categories = categoryRepository.findAll(pageable);

        List<CategoryDTO> content = categories.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                categories.getNumber(),
                categories.getSize(),
                categories.getTotalElements(),
                categories.getTotalPages(),
                categories.isLast()
        );
    }

    @Override
    public List<CategoryDTO> getAllCategoriesNoPage() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getParentCategories() {
        List<Category> parentCategories = categoryRepository.findAllParentCategories();

        return parentCategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getSubcategories(Integer parentId) {
        List<Category> subcategories = categoryRepository.findByParentId(parentId);

        return subcategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return convertToDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Check if category with same name already exists
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new IllegalArgumentException("Category already exists with name: " + categoryDTO.getName());
        }

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setImage(categoryDTO.getImage());

        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryDTO.getParentId()));
            category.setParent(parent);
        }

        if (categoryDTO.getStatus() != null) {
            category.setStatus(Category.CategoryStatus.valueOf(categoryDTO.getStatus()));
        }

        Category savedCategory = categoryRepository.save(category);

        return convertToDTO(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Integer id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if trying to update to a name that already exists for another category
        if (!category.getName().equals(categoryDTO.getName()) && categoryRepository.existsByName(categoryDTO.getName())) {
            throw new IllegalArgumentException("Category already exists with name: " + categoryDTO.getName());
        }

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setImage(categoryDTO.getImage());

        if (categoryDTO.getParentId() != null) {
            // Prevent circular reference
            if (categoryDTO.getParentId().equals(id)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }

            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryDTO.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        if (categoryDTO.getStatus() != null) {
            category.setStatus(Category.CategoryStatus.valueOf(categoryDTO.getStatus()));
        }

        Category updatedCategory = categoryRepository.save(category);

        return convertToDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        // Check if category exists
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if this category has subcategories
        if (!category.getSubcategories().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category with subcategories. Remove subcategories first.");
        }

        // You might want to check if there are any products with this category before deletion

        categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryDTO> getActiveCategories() {
        List<Category> categories = categoryRepository.findByStatus(Category.CategoryStatus.active);

        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Utility method to convert Entity to DTO
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImage(category.getImage());

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        dto.setStatus(category.getStatus().name());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        // Map subcategories (without their subcategories to avoid infinite recursion)
        List<CategoryDTO> subcategories = category.getSubcategories().stream()
                .map(sub -> {
                    CategoryDTO subDto = new CategoryDTO();
                    subDto.setId(sub.getId());
                    subDto.setName(sub.getName());
                    subDto.setDescription(sub.getDescription());
                    subDto.setImage(sub.getImage());
                    subDto.setParentId(category.getId());
                    subDto.setParentName(category.getName());
                    subDto.setStatus(sub.getStatus().name());
                    subDto.setCreatedAt(sub.getCreatedAt());
                    subDto.setUpdatedAt(sub.getUpdatedAt());
                    return subDto;
                })
                .collect(Collectors.toList());

        dto.setSubcategories(subcategories);

        return dto;
    }
}