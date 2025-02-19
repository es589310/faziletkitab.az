package com.example.elasticsearchservice.elastic;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryElastic {

    @Id
    private String categoryId;
//    private String categoryId;

    @Field(type = FieldType.Text, name = "categoryName")
    private String categoryName;

    @Field(type = FieldType.Text, name = "categoryDescription")
    private String categoryDescription;

}
