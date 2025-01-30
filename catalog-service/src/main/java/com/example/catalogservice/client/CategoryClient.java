package com.example.catalogservice.client;

import com.example.catalogservice.dto.CategoryDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;


public interface CategoryClient {

    @GetExchange("/api/categories/{categoryId}")
    CategoryDTO getCategoryId(@PathVariable Long categoryId);



}