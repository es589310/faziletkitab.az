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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final CatalogMapper catalogMapper;
    private final AuthorClient authorClient;
    private final CategoryClient categoryClient;
    private final ImageClient imageClient;


    public Page<CatalogResponse> allBooks(Pageable pageable) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("Kitablar 'page' üzrə göstərildi - 36");
            Page<Catalog> catalogPage = catalogRepository.findAll(pageable);

            Page<CatalogResponse> catalogResponsePage = catalogPage.map(catalog -> {
                CatalogResponse catalogResponse = catalogMapper.toResponse(catalog);
                try {
                    AuthorDTO author = authorClient.getAuthorById(catalog.getAuthorId());
                    if (author == null) {
                        log.error("Yazar məlumatı alınmır - 44: {}", author);
                        return catalogResponse;
                    }

                    catalogResponse.setAuthor(author);
                }catch (Exception e){
                    log.error("Yazar məlumatı alınmır - 50: {}", e.getMessage());
                    return catalogResponse;
                }


                try {
                    ImageResponse imageResponse = imageClient.getImageId(catalog.getId());
                    if (imageResponse == null || imageResponse.getImageUrl() == null) {
                        log.warn("Şəkillərin url-i tapılmır - 57! Response: {}", imageResponse);
                    }else {
                        catalogResponse.setImageUrl(imageResponse.getImageUrl());
                    }

                }catch (Exception e){
                    log.warn("Şəkillərin url-i tapılmır - 63! Response: {}", e.getMessage());
                    return catalogResponse;
                }


                try {
                    CategoryDTO categoryDTO = categoryClient.getCategoryId(catalog.getCategoryId());
                    if (categoryDTO == null){
                        log.error("Kateqoriya məlumatı alınmır - 70: {}", categoryDTO);
                    }else {
                        catalogResponse.setCategory(categoryDTO);
                    }
                }catch (Exception e){
                    log.warn("Kateqoriya tapılmır! Response - 74: {}", e.getMessage());
                    return catalogResponse;
                }
                return catalogResponse;
            });

            long endTime = System.currentTimeMillis();
            log.info("Kataloqdaki datalar sıralandı. Müddət: {} ms", (endTime - startTime));

            return catalogResponsePage;
        } catch (CatalogNotFoundException e) {
            log.error("Kateqoriya list halına keçmir: {}", e.getMessage());
            throw new RuntimeException("Kateqoriya list halına keçmədi!");
        }
    }


    public CatalogResponse findBookId(Long id) {
        try {
            Catalog catalog = catalogRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Kitab datası yoxdu!"));
            log.info("Kitab id-si ilə data tapıldı");

            CatalogResponse catalogResponse = catalogMapper.toResponse(catalog);
            try {
                AuthorDTO author = authorClient.getAuthorById(catalog.getAuthorId());
                if (author == null) {
                    log.error("Yazar məlumatı tapılmadı-85: {}", catalog.getAuthorId());
                }else {
                    catalogResponse.setAuthor(author);
                }
            }catch (Exception e){
                log.error("Yazar məlumatı tapılmadı-90: {}", e.getMessage());
                return catalogResponse;
            }


            try {
                ImageResponse imageResponse = imageClient.getImageId(id);
                if (imageResponse == null){
                    log.error("Şəkil url-i tapılmadı! Response-97: {}", imageResponse);
                }else {
                    catalog.setImageUrl(imageResponse.getImageUrl());
                }
            }catch (Exception e){
                log.error("Şəkil url-i tapılmadı! Response-102: {}", e.getMessage());
                return catalogResponse;
            }


            try {
                CategoryDTO categoryDTO = categoryClient.getCategoryId(catalog.getCategoryId());
                if (categoryDTO == null){
                    log.error("Kateqoriya id-si tapılmadı! Response-122: {}", categoryDTO);
                }else {
                    catalogResponse.setCategory(categoryDTO);
                }
            }catch (Exception e){
                log.warn("Kateqoriya id-si tapılmadı! Response-127: {}", e.getMessage());
                return catalogResponse;
            }


            catalogRepository.save(catalog);
            return catalogResponse;
        } catch (CatalogNotFoundException e) {
            log.error("Kataloq id ile məlumat tapılmadı: {}", id);
            throw new RuntimeException("Kataloq id-si ilə məlumat tapılmadı");
        }
    }

    public CatalogResponse addBook(CatalogRequest catalogRequest) {
        try {
            Catalog catalog = catalogMapper.toEntity(catalogRequest);
            Catalog savedCatalog = catalogRepository.save(catalog);
            log.info("Kitab məlumatı əlavə edildi");
            return catalogMapper.toResponse(savedCatalog);
        } catch (CatalogNotFoundException e) {
            log.error("Kitab məlumatı tapılmır ");
            throw new RuntimeException("Kitab əlavə oluna bilmir");
        }
    }

    public CatalogResponse updateBook(Long id, CatalogRequest catalogRequest) {
        try {
            Catalog existingCatalog = catalogRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Kitab tapılmır"));
            existingCatalog.setTitle(catalogRequest.getTitle());
            existingCatalog.setPrice(catalogRequest.getPrice());
            existingCatalog.setDescription(catalogRequest.getDescription());
            existingCatalog.setAuthorId(catalogRequest.getAuthorId());
            existingCatalog.setCategoryId(catalogRequest.getCategoryId());
            Catalog updatedCatalog = catalogRepository.save(existingCatalog);
            log.info("Kitab məlumatı yeniləndi");
            return catalogMapper.toResponse(updatedCatalog);
        } catch (CatalogNotFoundException e) {
            log.error("Kitab yenilənmədi");
            throw new RuntimeException("Kitab yenilənmədi");
        }
    }

    public void deleteBook(Long id) {
        try {
            Catalog existingCatalog = catalogRepository.findById(id)
                    .orElseThrow(() -> new CatalogNotFoundException("Kitap tapılmır"));
            catalogRepository.delete(existingCatalog);
            log.info("Kitab məlumatları silindi");
        } catch (CatalogNotFoundException e) {
            log.error("Kitab məlumatları silinmədi");
            throw new RuntimeException("Kitab məlumatları silinmədi");
        }
    }

    public AuthorDTO getAuthorDetails(Long authorId) {
        if (authorId == null){
            return null;
        }
        try {
            return authorClient.getAuthorById(authorId);
        } catch (Exception e) {
            log.error("Yazar məlumatı alınmadı: {}", authorId);
            throw new RuntimeException("Yazar məlumatı alanda xəta baş verdi!");
        }
    }

    public ImageResponse getImageDetails(Long imageId) {
        try {
            return imageClient.getImageId(imageId);
        } catch (Exception e) {
            log.error("Şəkil məlumatları görünmür: {}", imageId);
            throw new RuntimeException("Şəkil məlumatlarında xəta oldu");
        }
    }

    public CategoryDTO getCategoryDetails(Long categoryId) {
        try {
            return categoryClient.getCategoryId(categoryId);
        } catch (Exception e) {
            log.error("Kategoriya məlumatı alınmadı: {}", categoryId);
            throw new RuntimeException("Kategoriya məlumatı alarken xəta oldu");
        }
    }
}