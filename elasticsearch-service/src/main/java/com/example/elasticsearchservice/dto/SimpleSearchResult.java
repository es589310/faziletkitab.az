package com.example.elasticsearchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleSearchResult {
    private String type; // "CATALOG", "AUTHOR", "CATEGORY"
    private Long id;
    private String text; // title, name veya categoryName
}