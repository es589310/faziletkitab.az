package com.example.authorservice.response;


import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthorResponse{
    private Long authorId;
    private String name;
    private String biography;
    private String birthDate;
}
