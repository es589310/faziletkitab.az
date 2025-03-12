package com.example.elasticsearchservice.repository;

import com.example.elasticsearchservice.document.CategoryDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryElasticsearchRepository extends ElasticsearchRepository<CategoryDocument, Long> {
    // Basit aramalar
    List<CategoryDocument> findByCategoryNameContaining(String name);

    // Prefix (önek) araması
    List<CategoryDocument> findByCategoryNameStartingWith(String prefix);
    
    // Fuzzy arama (benzer kelimeler)
    @Query("{\"fuzzy\": {\"categoryName\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\"}}}")
    List<CategoryDocument> findByCategoryNameFuzzy(String name);
    
    // Multi-match arama (birden fazla alanda)
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"categoryName\", \"categoryDescription\"], \"fuzziness\": \"AUTO\"}}")
    List<CategoryDocument> searchInAllFields(String text);
}


