package com.example.catalogservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {

    private Long authorId;
    private String name;
    private String biography;
    private String birthDate;

}
