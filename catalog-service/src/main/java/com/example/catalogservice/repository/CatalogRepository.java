package com.example.catalogservice.repository;

import com.example.catalogservice.entity.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CatalogRepository extends JpaRepository<Catalog, Long> {
//    List<Catalog> findByCategoryId(Long categoryId);
//    List<Catalog> findByTitleContainingIgnoreCase(String title);
}
