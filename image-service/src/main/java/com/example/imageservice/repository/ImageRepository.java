package com.example.imageservice.repository;


import com.example.imageservice.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    //bir kitaba aid vizuallıqları gətirəcək
    Image findByBookId(Long bookId);
}
