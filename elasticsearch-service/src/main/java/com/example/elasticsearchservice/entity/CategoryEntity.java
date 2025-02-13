package com.example.elasticsearchservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "categories") // Elasticsearch'teki indeks adı
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {
    @Id
    private Long categoryId;
    private String categoryName;
    private String categoryDescription;
}
