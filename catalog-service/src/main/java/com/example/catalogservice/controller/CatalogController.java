package com.example.catalogservice.controller;


import com.example.catalogservice.dto.AuthorDTO;
import com.example.catalogservice.dto.CategoryDTO;
import com.example.catalogservice.exception.CatalogNotFoundException;
import com.example.catalogservice.request.CatalogRequest;
import com.example.catalogservice.response.CatalogResponse;
import com.example.catalogservice.response.ImageResponse;
import com.example.catalogservice.service.impl.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
@Slf4j
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getAllBooks(@RequestParam(defaultValue = "0") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size) {
        try {
            log.info("Kataloqun siyahılaşdırılması başladıldı. Səhifə: {}, Ölçüsü: {}", page, size);
            Pageable pageable = PageRequest.of(page, size);

            Page<CatalogResponse> catalogResponsePage = catalogService.allBooks(pageable);

            log.info("Kataloqun siyahılaşdırıldı. Kataloq sayı: {}", catalogResponsePage.getContent().size());
            return ResponseEntity.ok().body(catalogResponsePage);
        }catch (CatalogNotFoundException e){
            log.warn("Kataloq məlumatları tapılmadı - 41 : {}",e.getMessage());
            return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CatalogResponse findBookId(@PathVariable Long id){
        log.info("Kataloq məlumatı istənilir. Kataloq ID: {}", id);
        try {
            CatalogResponse catalogResponse = catalogService.findBookId(id);
            log.info("Kataloq məlumatı çağırıldı. Kataloq ID: {}", id);
            return catalogResponse;
        }catch (CatalogNotFoundException e){
            log.warn("Kataloq tapılmadı. Kataloq ID: {}. Error: {}", id, e.getMessage());
            throw e;
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogResponse addBook(@RequestBody CatalogRequest catalogRequest){
        log.info("Yeni Kataloq əlavə edilir. Kataloq adı: {}", catalogRequest.getTitle());
        CatalogResponse catalogResponse = catalogService.addBook(catalogRequest);
        log.info("Yeni Kataloq əlavə edildi. Kataloq adı: {}", catalogRequest.getTitle());
        return catalogResponse;
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CatalogResponse updateBook(@PathVariable Long id, @RequestBody CatalogRequest catalogRequest){
        log.info("Kataloq yenilənir. Kataloq ID: {}", id);
        CatalogResponse catalogResponse = catalogService.updateBook(id, catalogRequest);
        log.info("Kataloq yeniləndi. Kataloq ID: {}", id);
        return catalogResponse;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable Long id){
        log.info("Kataloq silinəcək. Kataloq ID: {}", id);
        catalogService.deleteBook(id);
        log.info("Kataloq silindi. Kataloq ID: {}", id);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<AuthorDTO> getAuthorDetails(@PathVariable Long authorId) {
        log.info("Yazar məlumatı istənilir. Yazar ID: {}", authorId);
        try {
            AuthorDTO authorDetails = catalogService.getAuthorDetails(authorId);
            log.info("Yazar məlumatı alındı. Yazar ID: {}", authorId);
            return ResponseEntity.ok(authorDetails);
        } catch (RuntimeException e) {
            log.warn("Yazar məlumatı alınmadı. Yazar ID: {}. Error: {}", authorId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<ImageResponse> getImageDetails(@PathVariable Long imageId) {
        log.info("Şəkil məlumatı istənilir. Şəkil ID: {}", imageId);
        try {
            ImageResponse imageDetails = catalogService.getImageDetails(imageId);
            log.info("Şəkil məlumatı alındı. Şəkil ID: {}", imageId);
            return ResponseEntity.ok(imageDetails);
        } catch (RuntimeException e) {
            log.warn("Şəkil məlumatı alınmadı. Şəkil ID: {}. Error: {}", imageId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategoryDetails(@PathVariable Long categoryId) {
        log.info("Kateqoriya məlumatı istənilir. Kateqoriya ID: {}", categoryId);
        try {
            CategoryDTO categoryDetails = catalogService.getCategoryDetails(categoryId);
            log.info("Kateqoriya məlumatı alındı. Kateqoriya ID: {}", categoryId);
            return ResponseEntity.ok(categoryDetails);
        } catch (RuntimeException e) {
            log.warn("Kateqoriya məlumatı alınmadı. Kateqoriya ID: {}. Error: {}", categoryId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}