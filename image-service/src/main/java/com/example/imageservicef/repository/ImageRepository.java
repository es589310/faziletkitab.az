package com.example.imageservicef.repository;


import com.example.imageservicef.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    //bir kitaba aid vizuallıqları gətirəcək
    Image findByBookId(Long bookId);
}
