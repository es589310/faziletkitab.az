package com.example.elasticsearchservice.client;

import com.example.elasticsearchservice.entity.CategoryEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


public interface CategoryClient {


    @GetMapping("/api/categories")
    List<CategoryEntity> getAllCategories();


}