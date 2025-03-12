package com.example.elasticsearchservice.client;

import com.example.elasticsearchservice.config.FeignClientConfig;
import com.example.elasticsearchservice.dto.response.AuthorResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
    name = "author-service",
    url = "${author.service.url}",
    configuration = FeignClientConfig.class
)
public interface AuthorClient {
    
    @GetMapping("/api/authors/getAllAuthors")
    @CircuitBreaker(name = "authorServiceCircuitBreaker", fallbackMethod = "getAllAuthorsFallback")
    ResponseEntity<List<AuthorResponseDto>> getAllAuthors();

    default ResponseEntity<List<AuthorResponseDto>> getAllAuthorsFallback(Throwable throwable) {
        return ResponseEntity.ok(List.of());
    }
} 