package com.example.imageservice.repository;


import com.example.imageservice.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT i from Image i WHERE i.bookId = :bookId ORDER BY i.uploadedAt DESC")
    Optional<Image> findByBookId(@Param("bookId") Long bookId);

}
