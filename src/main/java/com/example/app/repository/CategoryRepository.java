package com.example.app.repository;

import com.example.app.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findAllParentCategories();

    List<Category> findByParentId(Integer parentId);

    List<Category> findByStatus(Category.CategoryStatus status);
}