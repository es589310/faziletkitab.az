package com.example.elasticsearchservice.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(indexName = "catalogs")
public class CatalogDocument {
    @Id
    private String catalogId;

    @Field(type = FieldType.Text, name = "title")
    private String title;
} 