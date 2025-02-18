package com.example.elasticsearchservice.controller;

import com.example.elasticsearchservice.entity.CategoryEntity;
import com.example.elasticsearchservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/apis/category-searching")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/search")
    public ResponseEntity<?> searchCategory(@RequestParam String searchText) {
        try {
            List<CategoryEntity> result = categoryService.searchCategory(searchText);
            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Aradığınız kategori bulunamadı.");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Arama hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Bir hata oluştu. Lütfen tekrar deneyin.");
        }
    }

    @GetMapping
    public Iterable<CategoryEntity> findAll() {
        return categoryService.findAll();
    }

    @PostMapping("/add")
    public CategoryEntity addCategory(@RequestBody CategoryEntity categoryEntity) {
        return categoryService.insertCategory(categoryEntity);
    }

    @PutMapping("/update/{categoryId}")
    public CategoryEntity updateCategory(@RequestBody CategoryEntity categoryEntity, @PathVariable Long categoryId) {
        return categoryService.updateCategory(categoryEntity, categoryId);
    }

    @DeleteMapping("/delete/{categoryId}")
    public void deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }
}
