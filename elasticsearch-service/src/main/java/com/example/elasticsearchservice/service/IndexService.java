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
import com.example.elasticsearchservice.entity.AuthorEntity;
import com.example.elasticsearchservice.entity.CatalogEntity;
import com.example.elasticsearchservice.entity.CategoryEntity;
import com.example.elasticsearchservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexService {

    private final CategoryElasticsearchRepository categoryElasticsearchRepository;
    private final CatalogElasticsearchRepository catalogElasticsearchRepository;
    private final AuthorElasticsearchRepository authorElasticsearchRepository;

    private final AuthorRepository authorRepository;
    private final CatalogRepository catalogRepository;
    private final CategoryRepository categoryRepository;

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
            // Dış servisten kategori verilerini alıyoruz
            ResponseEntity<List<CategoryResponseDto>> response = categoryClient.getAllCategories();
            List<CategoryResponseDto> categories = response.getBody();

            if (categories != null && !categories.isEmpty()) {
                // Veriyi Elasticsearch'e kaydediyoruz
                List<CategoryDocument> documents = categories.stream()
                        .map(c -> new CategoryDocument(
                                c.getCategoryId().toString(),
                                c.getCategoryName()))
                        .collect(Collectors.toList());

                categoryElasticsearchRepository.saveAll(documents);
                log.info("Elasticsearch'e {} kategori kaydedildi.", documents.size());

                // Veritabanına da kaydediyoruz
                categoryRepository.saveAll(categories.stream()
                        .map(c -> new CategoryEntity(c.getCategoryId(), c.getCategoryName()))
                        .collect(Collectors.toList()));
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
                // Dış servisten katalog verilerini alıyoruz
                ResponseEntity<List<CatalogResponseDto>> response = catalogClient.getAllCatalogs(page, size);
                catalogList = response.getBody();

                if (catalogList != null && !catalogList.isEmpty()) {
                    // Veriyi Elasticsearch'e kaydediyoruz
                    List<CatalogDocument> documents = catalogList.stream()
                            .map(c -> new CatalogDocument(
                                    c.getCatalogId().toString(),
                                    c.getTitle()))
                            .collect(Collectors.toList());

                    catalogElasticsearchRepository.saveAll(documents);
                    log.info("Sayfa {}: Elasticsearch'e {} katalog kaydedildi.",
                            page, documents.size());

                    // Veritabanına da kaydediyoruz
                    catalogRepository.saveAll(catalogList.stream()
                            .map(c -> new CatalogEntity(c.getCatalogId(), c.getTitle()))
                            .collect(Collectors.toList()));
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
            // Dış servisten yazar verilerini alıyoruz
            ResponseEntity<List<AuthorResponseDto>> response = authorClient.getAllAuthors();
            if (response.getBody() != null) {
                // Veriyi Elasticsearch'e kaydediyoruz
                List<AuthorDocument> documents = response.getBody().stream()
                        .map(a -> new AuthorDocument(
                                a.getAuthorId().toString(),
                                a.getName()))
                        .collect(Collectors.toList());

                authorElasticsearchRepository.saveAll(documents);
                log.info("Elasticsearch'e {} yazar kaydedildi.", documents.size());

                // Veritabanına da kaydediyoruz
                authorRepository.saveAll(response.getBody().stream()
                        .map(a -> new AuthorEntity(a.getAuthorId(), a.getName()))
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error("Yazar senkronizasyonunda hata: ", e);
        }
    }
}
