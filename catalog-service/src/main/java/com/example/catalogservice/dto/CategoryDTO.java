package com.example.catalogservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryDTO {

    private Long categoryId;
    private String categoryName;
    private String categoryDescription;

}
