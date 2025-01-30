package com.example.categoryservice.controller;

import com.example.categoryservice.exception.CategoryNotFoundException;
import com.example.categoryservice.request.CategoryRequest;
import com.example.categoryservice.response.CategoryResponse;
import com.example.categoryservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<List<CategoryResponse>> getAllCategories(){
        try {
            log.info("Kateqoriya məlumatları servisdən çəkiləcək - 22");

            List<CategoryResponse> categoryResponses = categoryService.getAllCategories();
            log.info("Kateqoriya məlumatları servisdən çəkildi - 28");

            log.info("Kateqoriya məlumatları response edilir - 30");
            return new ResponseEntity<>(categoryResponses, HttpStatus.OK);
        }catch (CategoryNotFoundException e){
            log.error("Kateqoriya məlumatları response edilmədi - 35 {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getOneCategories(@PathVariable Long categoryId){
        try {
            log.info("Kateqoriya məlumatı servisdən çəkiləcək - 44");

            CategoryResponse categoryResponse = categoryService.getOneCategories(categoryId);
            log.info("Kateqoriya məlumatı servisdən çəkildi - 47");

            log.info("Kateqoriya məlumatı response edilir - 49");
            return new ResponseEntity<>(categoryResponse, HttpStatus.OK);

        }catch (CategoryNotFoundException e){
            log.error("Kateqoriya məlumatı response edilmədi - 35 {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping()
    public ResponseEntity<CategoryResponse> addCategoryInfo(@RequestBody CategoryRequest categoryRequest){
        try {
            log.info("Kateqoriya add üçün servisdən çəkiləcək - 60");

            CategoryResponse categoryResponse = categoryService.addCategoryInfo(categoryRequest);
            log.info("Kateqoriya add üçün servisdən çəkildi - 63");

            log.info("Kateqoriya add üçün response edilir - 65");
            return new ResponseEntity<>(categoryResponse,HttpStatus.CREATED);
        }catch (CategoryNotFoundException e){
            log.error("Kateqoriya məlumatı response edilmədi - 68 {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategoryInfo(@PathVariable Long categoryId,
                                                               @RequestBody CategoryRequest categoryRequest){
        try {
            log.info("Kateqoriya yenilənmək üçün servisdən çəkiləcək - 77");

            CategoryResponse categoryResponse = categoryService.updateCategoryInfo(categoryId,categoryRequest);
            log.info("Kateqoriya yenilənmək üçün servisdən çəkildi - 80");

            log.info("Kateqoriya yenilənmək üçün response edilir - 82");
            return new ResponseEntity<>(categoryResponse,HttpStatus.OK);
        }catch (CategoryNotFoundException e){
            log.error("Kateqoriya məlumatı response edilmədi - 85 {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategoryInfo(@PathVariable Long categoryId){
        try {
            log.info("Kateqoriya silinmək üçün servisdən çəkiləcək - 94");

            categoryService.deleteCategoryInfo(categoryId);
            log.info("Hansı kateqoriyanın silinməsi təyin edildi - 97");

            log.info("Kateqoriya silinmək üçün response edilir - 99");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (CategoryNotFoundException e){
            log.error("Kateqoriya məlumatı response edilmədi - 102 {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}























