package com.example.elasticsearchservice.client;

import com.example.elasticsearchservice.dto.CategoryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface CategoryClient {

    Logger log = LoggerFactory.getLogger(CategoryClient.class);

    @GetExchange("/api/categories")
//    @CircuitBreaker(name = "category", fallbackMethod = "fallbackMethod")
//    @Retry(name = "category")
    List<CategoryDTO> getAllCategories();
//    CategoryDTO getCategoryId(@PathVariable Long categoryId);

    default CategoryDTO fallbackMethod(Long id, Throwable throwable) {
        log.info("Cannot get category id {}, failure reason: {}", id, throwable.getMessage());
        return new CategoryDTO(id, "Unknown", "Unknown");
    }
}