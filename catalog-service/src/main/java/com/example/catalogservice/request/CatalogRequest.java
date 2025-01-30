package com.example.catalogservice.request;

import lombok.Data;

@Data
public class CatalogRequest {
    private String title;
    private String description;
    private Long categoryId;
    private Long authorId;
    private Double price;
    private String publishedDate;
}
