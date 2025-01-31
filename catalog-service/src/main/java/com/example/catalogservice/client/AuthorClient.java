package com.example.catalogservice.client;

import groovy.util.logging.Slf4j;
import com.example.catalogservice.dto.AuthorDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

@Slf4j
public interface AuthorClient {

    Logger log = LoggerFactory.getLogger(AuthorClient.class);

    @GetExchange("/api/authors/{authorId}")
    @CircuitBreaker(name = "author", fallbackMethod = "fallbackMethod")
    @Retry(name = "author")
    AuthorDTO getAuthorById(@PathVariable("authorId") Long authorId);

    default AuthorDTO fallbackMethod(Long authorId, Throwable throwable) {
        log.info("Cannot get author by id {}, failure reason: {}", authorId, throwable.getMessage());
        return new AuthorDTO(authorId, "Unknown", "No biography available", "N/A");
    }
}

