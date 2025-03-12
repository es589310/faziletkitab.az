package com.example.elasticsearchservice.dto;

import com.example.elasticsearchservice.document.AuthorDocument;
import com.example.elasticsearchservice.document.CatalogDocument;
import com.example.elasticsearchservice.document.CategoryDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private List<CatalogDocument> catalogs;
    private List<AuthorDocument> authors;
    private List<CategoryDocument> categories;
} 