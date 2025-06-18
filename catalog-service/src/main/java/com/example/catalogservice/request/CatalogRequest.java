package com.example.catalogservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CatalogRequest {

    @NotBlank(message = "Başlıq boş ola bilməz")
    private String title;
    private String description;
    @Positive(message = "Qiymət müsbət olmalıdır")
    private Double price;
    private String publishedDate;
    @NotNull(message = "Müəllif ID-si boş ola bilməz")
    private Long authorId;
    @NotNull(message = "Kateqoriya ID-si boş ola bilməz")
    private Long categoryId;

}