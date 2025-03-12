package com.example.elasticsearchservice.controller;

import com.example.elasticsearchservice.service.IndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/index")
@Slf4j
@Tag(name = "Index Operations", description = "Elasticsearch indeksleme işlemleri")
public class IndexController {
    private final IndexService indexService;

    @PostMapping("/sync/all")
    @Operation(summary = "Tüm verileri senkronize et")
    public ResponseEntity<String> syncAllData() {
        log.info("Tüm veriler için senkronizasyon başlatılıyor...");
        indexService.syncAllData();
        return ResponseEntity.ok("Senkronizasyon başlatıldı");
    }

    @PostMapping("/sync/categories")
    @Operation(summary = "Kategorileri senkronize et")
    public ResponseEntity<String> syncCategories() {
        log.info("Kategori senkronizasyonu başlatılıyor...");
        indexService.syncCategories();
        return ResponseEntity.ok("Kategori senkronizasyonu başlatıldı");
    }

    @PostMapping("/sync/catalogs")
    @Operation(summary = "Katalogları senkronize et")
    public ResponseEntity<String> syncCatalogs() {
        log.info("Katalog senkronizasyonu başlatılıyor...");
        indexService.syncCatalogs();
        return ResponseEntity.ok("Katalog senkronizasyonu başlatıldı");
    }

    @PostMapping("/sync/authors")
    @Operation(summary = "Yazarları senkronize et")
    public ResponseEntity<String> syncAuthors() {
        log.info("Yazar senkronizasyonu başlatılıyor...");
        indexService.syncAuthors();
        return ResponseEntity.ok("Yazar senkronizasyonu başlatıldı");
    }
}
