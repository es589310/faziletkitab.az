package com.example.elasticsearchservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CatalogResponseDto {
    private Long catalogId;
    private String title;
} 