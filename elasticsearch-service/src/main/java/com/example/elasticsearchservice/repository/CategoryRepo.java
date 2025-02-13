package com.example.elasticsearchservice.repository;

import com.example.elasticsearchservice.entity.CategoryEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepo extends ElasticsearchRepository<CategoryEntity, Long> {

}
