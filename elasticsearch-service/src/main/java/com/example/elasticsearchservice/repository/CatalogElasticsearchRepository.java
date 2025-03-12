package com.example.elasticsearchservice.repository;

import com.example.elasticsearchservice.document.CatalogDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogElasticsearchRepository extends ElasticsearchRepository<CatalogDocument, Long> {
    // Temel aramalar
    List<CatalogDocument> findByNameContaining(String name);
    List<CatalogDocument> findByDescriptionContaining(String description);
    
    // Kategori ve yazar bazlı aramalar
    List<CatalogDocument> findByCategoryId(String categoryId);
    List<CatalogDocument> findByAuthorId(String authorId);
    
    // Fuzzy arama
    @Query("{\"fuzzy\": {\"name\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\"}}}")
    List<CatalogDocument> findByNameFuzzy(String name);
    
    // Çoklu alan araması
    @Query("{\"bool\": {\"should\": [{\"match\": {\"name\": \"?0\"}}, {\"match\": {\"description\": \"?0\"}}]}}")
    List<CatalogDocument> searchInAllFields(String text);
    
    // Kategori ve metin bazlı kombine arama
    @Query("{\"bool\": {\"must\": [{\"match\": {\"categoryId\": \"?0\"}}, {\"multi_match\": {\"query\": \"?1\", \"fields\": [\"name\", \"description\"]}}]}}")
    List<CatalogDocument> findByCategoryIdAndText(String categoryId, String text);
} 