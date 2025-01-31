package com.example.catalogservice.client;

import com.example.catalogservice.dto.CategoryDTO;
import groovy.util.logging.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

@Slf4j
public interface CategoryClient {

    Logger log = LoggerFactory.getLogger(CategoryClient.class);

    @GetExchange("/api/categories/{categoryId}")
    @CircuitBreaker(name = "category", fallbackMethod = "fallbackMethod")
    @Retry(name = "category")
    CategoryDTO getCategoryId(@PathVariable Long categoryId);

    default CategoryDTO fallbackMethod(Long id, Throwable throwable) {
        log.info("Cannot get category id {}, failure reason: {}", id, throwable.getMessage());
        return new CategoryDTO(id, "Unknown", "Unknown");
    }
}