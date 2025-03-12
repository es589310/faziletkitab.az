package com.example.elasticsearchservice.client;

import com.example.elasticsearchservice.config.FeignClientConfig;
import com.example.elasticsearchservice.dto.response.CategoryResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
    name = "category-service",
    url = "${category.service.url}",
    configuration = FeignClientConfig.class
)
public interface CategoryClient {
    
    @GetMapping("/api/categories/getAllCategories")
    @CircuitBreaker(name = "categoryServiceCircuitBreaker", fallbackMethod = "getAllCategoriesFallback")
    ResponseEntity<List<CategoryResponseDto>> getAllCategories();

    default ResponseEntity<List<CategoryResponseDto>> getAllCategoriesFallback(Throwable throwable) {
        return ResponseEntity.ok(List.of());
    }
}

