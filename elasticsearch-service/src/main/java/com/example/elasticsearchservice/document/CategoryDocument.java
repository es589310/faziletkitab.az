package com.example.elasticsearchservice.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "categories")
public class CategoryDocument {
    @Id
    private String categoryId;

    @Field(type = FieldType.Text, name = "categoryName")
    private String categoryName;
}
