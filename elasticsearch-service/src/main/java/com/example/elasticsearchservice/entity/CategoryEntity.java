package com.example.elasticsearchservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {
    @Id
    private Long categoryId;

    @Field(type = FieldType.Text, name = "categoryName")
    private String categoryName;

    @Field(type = FieldType.Text, name = "categoryDescription")
    private String categoryDescription;
}
