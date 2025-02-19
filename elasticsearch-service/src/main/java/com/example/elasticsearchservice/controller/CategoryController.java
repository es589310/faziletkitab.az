package com.example.elasticsearchservice.controller;

import com.example.elasticsearchservice.elastic.CategoryElastic;
import com.example.elasticsearchservice.entity.CategoryEntity;
import com.example.elasticsearchservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/apis/category-searching")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/search")
    public String syncCategories(@RequestBody List<CategoryEntity> categories) {
        categoryService.saveCategory(categories);
        return "Sinxronizasiyalaşdırma!";
    }
//
//    @GetMapping
//    public List<CategoryElastic> search(@RequestParam String query) {
//        return categoryService.searchCategories(query);
//    }
}
