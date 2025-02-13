package com.example.elasticsearchservice.service;

import com.example.elasticsearchservice.client.CategoryClient;
import com.example.elasticsearchservice.dto.CategoryDTO;
import com.example.elasticsearchservice.entity.CategoryEntity;
import com.example.elasticsearchservice.repository.CategoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryClient categoryClient;
    private final CategoryRepo categoryRepo;

    public Iterable<CategoryEntity> findAll() {
        // Tüm kategorileri al
        List<CategoryDTO> categoryDTOs = categoryClient.getAllCategories();

        // CategoryDTO listesini CategoryEntity listesine dönüştür
        List<CategoryEntity> categoryEntities = new ArrayList<>();
        for (CategoryDTO categoryDTO : categoryDTOs) {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setCategoryId(categoryDTO.getCategoryId());
            categoryEntity.setCategoryName(categoryDTO.getCategoryName());
            categoryEntity.setCategoryDescription(categoryDTO.getCategoryDescription());
            categoryEntities.add(categoryEntity);
        }

        return categoryEntities;
    }



    public CategoryEntity insertCategory(CategoryEntity categoryEntity) {
        return categoryRepo.save(categoryEntity);
    }

    public CategoryEntity updateCategory(CategoryEntity categoryEntity, Long categoryId) {
        CategoryEntity category = categoryRepo.findById(categoryId).orElse(null);
        if (category != null) {
            category.setCategoryName(categoryEntity.getCategoryName());
            category.setCategoryDescription(categoryEntity.getCategoryDescription());
            return categoryRepo.save(category);
        }
        return null;
    }

    public void deleteCategory(Long categoryId) {
        categoryRepo.deleteById(categoryId);
    }

    public CategoryEntity convertToEntity(CategoryDTO categoryDTO) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryId(categoryDTO.getCategoryId());
        categoryEntity.setCategoryName(categoryDTO.getCategoryName());
        categoryEntity.setCategoryDescription(categoryDTO.getCategoryDescription());
        return categoryEntity;
    }

//    @CircuitBreaker(name = "categoryClient", fallbackMethod = "fallbackMethod")
//    public CategoryDTO getCategoryById(Long categoryId) {
//        return categoryClient.getCategoryId(categoryId);
//    }
//
//    public CategoryDTO fallbackMethod(Long categoryId, Throwable throwable) {
//        return new CategoryDTO(categoryId, "Unknown", "Unknown");
//    }

}

