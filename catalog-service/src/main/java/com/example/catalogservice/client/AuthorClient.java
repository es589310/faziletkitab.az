package com.example.catalogservice.client;

import com.example.catalogservice.dto.AuthorDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface AuthorClient {

    @GetExchange("/api/authors/{authorId}")
    AuthorDTO getAuthorById(@PathVariable("authorId") Long authorId);
}

