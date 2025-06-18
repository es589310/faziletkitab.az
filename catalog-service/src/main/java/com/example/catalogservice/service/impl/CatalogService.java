package com.example.catalogservice.service.impl;

import com.example.catalogservice.client.AuthorClient;
import com.example.catalogservice.client.CategoryClient;
import com.example.catalogservice.client.ImageClient;
import com.example.catalogservice.dto.AuthorDTO;
import com.example.catalogservice.dto.CategoryDTO;
import com.example.catalogservice.entity.Catalog;
import com.example.catalogservice.exception.CatalogNotFoundException;
import com.example.catalogservice.mapper.CatalogMapper;
import com.example.catalogservice.repository.CatalogRepository;
import com.example.catalogservice.request.CatalogRequest;
import com.example.catalogservice.response.CatalogResponse;
import com.example.catalogservice.response.ImageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final CatalogMapper catalogMapper;
    private final AuthorClient authorClient;
    private final CategoryClient categoryClient;
    private final ImageClient imageClient;

    private CatalogResponse mapToResponseWithDetails(Catalog catalog) {
        CatalogResponse response = catalogMapper.toResponse(catalog);


        AuthorDTO author = authorClient.getAuthorById(catalog.getAuthorId());
        response.setAuthor(author);

        CategoryDTO category = categoryClient.getCategoryId(catalog.getCategoryId());
            response.setCategory(category);

        ImageResponse image = imageClient.getImageByBookId(catalog.getId());
            if (image != null) {
                response.setImageUrl(image.getImageUrl());
            }

        return response;
    }


    @Cacheable("books")
    public Page<CatalogResponse> allBooks(Pageable pageable) {
        long startTime = System.currentTimeMillis();
        log.info("Fetching all books, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Catalog> catalogPage = catalogRepository.findAll(pageable);
        Page<CatalogResponse> catalogResponsePage = catalogPage.map(this::mapToResponseWithDetails);

        long endTime = System.currentTimeMillis();
        log.info("Books fetched successfully. Duration: {} ms, Total books: {}", endTime - startTime, catalogResponsePage.getTotalElements());
        return catalogResponsePage;
    }


    public CatalogResponse findBookId(Long id) {
        Catalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new CatalogNotFoundException("Kitab tapılmadı: " + id));
        return mapToResponseWithDetails(catalog);
    }


    public CatalogResponse addBook(@Valid CatalogRequest catalogRequest) {
        Catalog catalog = catalogMapper.toEntity(catalogRequest);
        Catalog savedCatalog = catalogRepository.save(catalog);
        log.info("Book added: {}", savedCatalog.getTitle());
        return mapToResponseWithDetails(savedCatalog);
    }


    public CatalogResponse updateBook(Long id, @Valid CatalogRequest catalogRequest) {
        Catalog existingCatalog = catalogRepository.findById(id)
                .orElseThrow(() -> new CatalogNotFoundException("Kitab tapılmadı: " + id));
        existingCatalog.setTitle(catalogRequest.getTitle());
        existingCatalog.setPrice(catalogRequest.getPrice());
        existingCatalog.setDescription(catalogRequest.getDescription());
        existingCatalog.setAuthorId(catalogRequest.getAuthorId());
        existingCatalog.setCategoryId(catalogRequest.getCategoryId());
        Catalog updatedCatalog = catalogRepository.save(existingCatalog);
        log.info("Book updated: {}", updatedCatalog.getTitle());
        return mapToResponseWithDetails(updatedCatalog);
    }


    public void deleteBook(Long id) {
        Catalog existingCatalog = catalogRepository.findById(id)
                .orElseThrow(() -> new CatalogNotFoundException("Kitab tapılmadı: " + id));
        catalogRepository.delete(existingCatalog);
        log.info("Book deleted: {}", id);
    }




    public AuthorDTO getAuthorDetails(Long authorId) {
        if (authorId == null) {
            throw new IllegalArgumentException("Müəllif ID-si boş ola bilməz");
        }
        try {
            AuthorDTO author = authorClient.getAuthorById(authorId);
            if (author == null) {
                throw new CatalogNotFoundException("Müəllif tapılmadı: " + authorId);
            }
            return author;
        } catch (Exception e) {
            log.error("Failed to fetch author details for authorId: {}. Error: {}", authorId, e.getMessage());
            throw new CatalogNotFoundException("Müəllif məlumatı alınmadı: " + authorId);
        }
    }

    public ImageResponse getImageDetails(Long imageId) {
        if (imageId == null) {
            throw new IllegalArgumentException("Şəkil ID-si boş ola bilməz");
        }
        try {
            ImageResponse image = imageClient.getImageByBookId(imageId);
            if (image == null) {
                throw new CatalogNotFoundException("Şəkil tapılmadı: " + imageId);
            }
            return image;
        } catch (Exception e) {
            log.error("Failed to fetch image details for imageId: {}. Error: {}", imageId, e.getMessage());
            throw new CatalogNotFoundException("Şəkil məlumatı alınmadı: " + imageId);
        }
    }

    public CategoryDTO getCategoryDetails(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Kateqoriya ID-si boş ola bilməz");
        }
        try {
            CategoryDTO category = categoryClient.getCategoryId(categoryId);
            if (category == null) {
                throw new CatalogNotFoundException("Kateqoriya tapılmadı: " + categoryId);
            }
            return category;
        } catch (Exception e) {
            log.error("Failed to fetch category details for categoryId: {}. Error: {}", categoryId, e.getMessage());
            throw new CatalogNotFoundException("Kateqoriya məlumatı alınmadı: " + categoryId);
        }
    }
}