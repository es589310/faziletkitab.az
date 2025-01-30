package com.example.catalogservice.response;

import com.example.catalogservice.dto.AuthorDTO;
import com.example.catalogservice.dto.CategoryDTO;
import lombok.Data;

@Data
public class CatalogResponse {
    private Long id;
    private String title;
    private String description;
    private AuthorDTO author;
    private CategoryDTO category;
    private Double price;
    private String imageUrl;
    private String publishedDate;
}
