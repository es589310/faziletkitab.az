package com.example.elasticsearchservice.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SimpleSearchResult {
    private String type; // "CATALOG", "AUTHOR", "CATEGORY"
    private String id;  // Burayı String olarak değiştirin
    private String text; // title, name veya categoryName
}
