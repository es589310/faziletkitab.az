package com.example.elasticsearchservice.controller;

import com.example.elasticsearchservice.entity.CategoryEntity;
import com.example.elasticsearchservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/apis/category-searching")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Iterable<CategoryEntity> findAll() {
        // Tüm kategorileri veritabanından veya başka bir kaynaktan al
        return categoryService.findAll();
    }

    // Kategori eklemek için
    @PostMapping("/add")
    public CategoryEntity addCategory(@RequestBody CategoryEntity categoryEntity) {
        return categoryService.insertCategory(categoryEntity);
    }

    // Kategori güncellemek için
    @PutMapping("/update/{categoryId}")
    public CategoryEntity updateCategory(@RequestBody CategoryEntity categoryEntity, @PathVariable Long categoryId) {
        return categoryService.updateCategory(categoryEntity, categoryId);
    }

    // Kategori silmek için
    @DeleteMapping("/delete/{categoryId}")
    public void deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }
}
