package com.example.elasticsearchservice.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.example.elasticsearchservice.entity.CategoryEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public interface CategoryRepo extends ElasticsearchRepository<CategoryEntity, Long>{

}


