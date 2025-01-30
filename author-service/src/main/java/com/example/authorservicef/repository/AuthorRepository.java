package com.example.authorservicef.repository;

import com.example.authorservicef.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    Author findByAuthorId(Long authorId);
}
