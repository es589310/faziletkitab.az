package com.example.elasticsearchservice.repository;

import com.example.elasticsearchservice.document.AuthorDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends ElasticsearchRepository<AuthorDocument, Long> {
    // Temel aramalar
    List<AuthorDocument> findByNameContaining(String name);
    List<AuthorDocument> findByBiographyContaining(String biography);
    
    // Fuzzy arama
    @Query("{\"fuzzy\": {\"name\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\"}}}")
    List<AuthorDocument> findByNameFuzzy(String name);
    
    // Çoklu alan araması
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name\", \"biography\"], \"fuzziness\": \"AUTO\"}}")
    List<AuthorDocument> searchInAllFields(String text);
    
    // İsim prefix araması
    List<AuthorDocument> findByNameStartingWith(String prefix);
} 