package com.example.elasticsearchservice.client;

import com.example.elasticsearchservice.config.FeignClientConfig;
import com.example.elasticsearchservice.dto.response.CatalogResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "catalog-service",
        url = "${catalog.service.url}",
        configuration = FeignClientConfig.class
)
public interface CatalogClient {

    @GetMapping("/api/catalogs/getAllCatalogs")
    @CircuitBreaker(name = "catalogServiceCircuitBreaker", fallbackMethod = "getAllCatalogsFallback")
    ResponseEntity<List<CatalogResponseDto>> getAllCatalogs(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    default ResponseEntity<List<CatalogResponseDto>> getAllCatalogsFallback(Throwable throwable) {
        return ResponseEntity.ok(List.of());
    }
}
