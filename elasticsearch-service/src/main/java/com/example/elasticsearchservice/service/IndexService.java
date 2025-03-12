package com.example.elasticsearchservice.service;

import com.example.elasticsearchservice.client.AuthorClient;
import com.example.elasticsearchservice.client.CatalogClient;
import com.example.elasticsearchservice.client.CategoryClient;
import com.example.elasticsearchservice.document.AuthorDocument;
import com.example.elasticsearchservice.document.CatalogDocument;
import com.example.elasticsearchservice.document.CategoryDocument;
import com.example.elasticsearchservice.dto.response.AuthorResponseDto;
import com.example.elasticsearchservice.dto.response.CategoryResponseDto;
import com.example.elasticsearchservice.dto.response.CatalogResponseDto;
import com.example.elasticsearchservice.repository.AuthorRepository;
import com.example.elasticsearchservice.repository.CatalogRepository;
import com.example.elasticsearchservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexService {
    private final CategoryRepository categoryRepository;
    private final CatalogRepository catalogRepository;
    private final AuthorRepository authorRepository;
    
    private final CategoryClient categoryClient;
    private final CatalogClient catalogClient;
    private final AuthorClient authorClient;

    // Tüm verileri senkronize et
    @Scheduled(fixedRate = 3600000) // Her saat başı
    public void syncAllData() {
        syncCategories();
        syncCatalogs();
        syncAuthors();
    }

    // Category senkronizasyonu
    public void syncCategories() {
        log.info("Kategori senkronizasyonu başlıyor...");
        try {
            ResponseEntity<List<CategoryResponseDto>> response = categoryClient.getAllCategories();
            List<CategoryResponseDto> categories = response.getBody();
            
            if (categories != null && !categories.isEmpty()) {
                List<CategoryDocument> documents = categories.stream()
                    .map(c -> new CategoryDocument(
                        c.getCategoryId(),
                        c.getCategoryName()))
                    .toList();

                categoryRepository.saveAll(documents);
                log.info("Elasticsearch'e {} kategori kaydedildi.", documents.size());
            }
        } catch (Exception e) {
            log.error("Kategori senkronizasyonunda hata: ", e);
        }
    }

    // Catalog senkronizasyonu
    public void syncCatalogs() {
        log.info("Katalog senkronizasyonu başlıyor...");
        try {
            int page = 0;
            int size = 100;
            List<CatalogResponseDto> catalogList;

            do {
                // Bu sefer page ve size parametreleriyle çağrılacak
                ResponseEntity<List<CatalogResponseDto>> response = catalogClient.getAllCatalogs(page, size);
                catalogList = response.getBody();

                if (catalogList != null && !catalogList.isEmpty()) {
                    List<CatalogDocument> documents = catalogList.stream()
                            .map(c -> new CatalogDocument(
                                    c.getCatalogId(),
                                    c.getTitle()))
                            .collect(Collectors.toList());

                    catalogRepository.saveAll(documents);
                    log.info("Sayfa {}: Elasticsearch'e {} katalog kaydedildi.",
                            page, documents.size());
                }

                page++;
            } while (catalogList != null && !catalogList.isEmpty());

        } catch (Exception e) {
            log.error("Katalog senkronizasyonunda hata: ", e);
        }
    }

    // Author senkronizasyonu
    @Scheduled(fixedRate = 300000) // 5 dakikada bir
    public void syncAuthors() {
        try {
            ResponseEntity<List<AuthorResponseDto>> response = authorClient.getAllAuthors();
            if (response.getBody() != null) {
                List<AuthorDocument> documents = response.getBody().stream()
                    .map(a -> new AuthorDocument(a.getAuthorId(), a.getName()))
                    .collect(Collectors.toList());
                
                authorRepository.saveAll(documents);
                log.info("Elasticsearch'e {} yazar kaydedildi.", documents.size());
            }
        } catch (Exception e) {
            log.error("Yazar senkronizasyonunda hata: ", e);
        }
    }
}
