package com.example.elasticsearchservice.repository;

import com.example.elasticsearchservice.elastic.CategoryElastic;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryElasticsearchRepo extends ElasticsearchRepository<CategoryElastic, String> {

//    List<CategoryEntity> findByCategoryName(String categoryName);

    List<CategoryElastic> findByCategoryNameContainingIgnoreCase(String query);

}
