package com.example.elasticsearchservice.repository;

import com.example.elasticsearchservice.entity.Catalog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogRepository extends ElasticsearchRepository<Catalog, Long> {
    List<Catalog> findByName(String name);
}
