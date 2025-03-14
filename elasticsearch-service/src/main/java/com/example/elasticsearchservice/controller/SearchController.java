package com.example.elasticsearchservice.controller;

import com.example.elasticsearchservice.document.AuthorDocument;
import com.example.elasticsearchservice.document.CatalogDocument;
import com.example.elasticsearchservice.document.CategoryDocument;
import com.example.elasticsearchservice.dto.SearchResponse;
import com.example.elasticsearchservice.dto.request.AuthorRequestDto;
import com.example.elasticsearchservice.dto.request.CatalogRequestDto;
import com.example.elasticsearchservice.dto.response.CategoryResponseDto;
import com.example.elasticsearchservice.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.directory.SearchResult;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Search Operations", description = "Elasticsearch arama işlemleri")
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Arama yapılıyor: query={}", query);
        return ResponseEntity.ok(searchService.search(query, page, size));
    }

    @GetMapping("/all")
    @Operation(summary = "Tüm alanlarda ara")
    public ResponseEntity<SearchResult> searchAll(@RequestParam String query) {
        log.info("Tüm alanlarda arama yapılıyor. Query: {}", query);
        return ResponseEntity.ok(searchService.searchAll(query));
    }

    @GetMapping("/categories")
    @Operation(summary = "Kategorilerde ara")
    public ResponseEntity<List<CategoryResponseDto>> searchCategories(@RequestParam String categoryName) {
        log.info("Kategori araması yapılıyor: categoryName={}", categoryName);
        return ResponseEntity.ok(searchService.searchCategories(categoryName));
    }

    @GetMapping("/categories/fuzzy")
    @Operation(summary = "Kategorilerde bulanık arama yap")
    public ResponseEntity<List<CategoryDocument>> searchCategoriesFuzzy(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchCategoriesFuzzy(query));
    }

    @PostMapping("/catalogs/search")
    public ResponseEntity<List<CatalogDocument>> searchCatalogs(@RequestBody CatalogRequestDto request) {
        log.info("Katalog araması yapılıyor: request={}", request);
        return ResponseEntity.ok(searchService.searchWithCatalogRequest(request));
    }

    @GetMapping("/catalogs/by-category")
    @Operation(summary = "Belirli kategorideki kataloglarda ara")
    public ResponseEntity<List<CatalogDocument>> searchCatalogsByCategory(
            @RequestParam Long categoryId,
            @RequestParam String query) {
        return ResponseEntity.ok(searchService.searchCatalogsByCategory(String.valueOf(categoryId), query));
    }

    @PostMapping("/authors/search")
    public ResponseEntity<List<AuthorDocument>> searchAuthors(@RequestBody AuthorRequestDto request) {
        log.info("Yazar araması yapılıyor: request={}", request);
        return ResponseEntity.ok(searchService.searchWithAuthorRequest(request));
    }

    @GetMapping("/authors/fuzzy")
    @Operation(summary = "Yazarlarda bulanık arama yap")
    public ResponseEntity<List<AuthorDocument>> searchAuthorsFuzzy(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchAuthorsFuzzy(query));
    }
} 