package com.example.categoryservice.service;

import com.example.categoryservice.entity.Category;
import com.example.categoryservice.exception.CategoryNotFoundException;
import com.example.categoryservice.mapper.CategoryMapper;
import com.example.categoryservice.repository.CategoryRepository;
import com.example.categoryservice.request.CategoryRequest;
import com.example.categoryservice.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> getAllCategories(){
        try {
            log.info("Kateqoriya üzrə iş gedir - 23");
            return categoryRepository.findAll().stream()
                    .map(category -> new CategoryResponse(
                            category.getCategoryId(),
                            category.getCategoryName(),
                            category.getCategoryDescription()
                    ))
                    .toList();
        }catch (CategoryNotFoundException e){
            log.error("Kateqoriyanın çağırıldığı zaman error baş verdi! - 32 : {}",e.getMessage());
            throw new RuntimeException("CategoryService - 33");
        }
    }

    public CategoryResponse getOneCategories(Long categoryId){
        try {
            log.info("Kategoriya id-ə görə çağırılmağa hazırdır!-41");
            Category category = categoryRepository.findByCategoryId(categoryId);
            log.info("Kategoriya id-ə görə çağırıldı hazırdır!-43");
            return new CategoryResponse(category.getCategoryId(),category.getCategoryName(),category.getCategoryDescription());
        }catch (CategoryNotFoundException e){
            log.error("Kateqoriyanı çağırarkən xəta oldu -46: {}",e.getMessage());
            throw new RuntimeException("Kateqoriya id-ə görə çağırıla bilinmir!-47");
        }
    }

    public CategoryResponse addCategoryInfo(CategoryRequest categoryRequest){
        try {
            log.info("Kategoriya əlavə edilmək üçün işə başlanılır!-53");
            Category addCategory = categoryMapper.toEntity(categoryRequest);
            log.info("Kategoriya mapper ilə requestdən entity-ə ötürüldü - 55");
            Category saveCategory = categoryRepository.save(addCategory);
            log.info("Kategoriya repoya save olundu - 55");
            return categoryMapper.toResponse(saveCategory);
        }catch (Exception e){
            log.error("Kateqoriyanı əlavə edərkən xəta oldu - 63: {}",e.getMessage());
            throw new RuntimeException("Kateqoriya əlavə edilmir! - 64");
        }
    }

    public CategoryResponse updateCategoryInfo(Long categoryId, CategoryRequest categoryRequest){
        try {
            log.info("Kategoriya yenilənmək üçün işə başlanılır!-70");

            Category updateCategory = categoryRepository.findByCategoryId(categoryId);
            log.info("Kategoriya repodan alınan datalar set olunmağa hazırdır! - 73");

            updateCategory.setCategoryDescription(categoryRequest.getCategoryDescription());
            updateCategory.setCategoryName(categoryRequest.getCategoryName());
            log.info("Kategoriya dataları set edildi! - 77");

            Category savedCategory = categoryRepository.save(updateCategory);
            log.info("Kategoriya dataları yenidən repoya save olundu! - 80");

            return categoryMapper.toResponse(savedCategory);
        }catch (CategoryNotFoundException e){
            log.error("Kateqoriyanı yenilənərkən xəta oldu - 84: {}",e.getMessage());
            throw new RuntimeException("Kateqoriya yenilənmir! - 85");
        }
    }

    public void deleteCategoryInfo(Long categoryId){
        try {
            log.info("Silinmə prosesi başlayır!- 91");

            Category findCategory = categoryRepository.findByCategoryId(categoryId);
            log.info("Silinmək üçün kateqoriya tapıldı - 94");

            categoryRepository.delete(findCategory);
            log.info("Kateqoriya silindi - 97");
        }catch (CategoryNotFoundException e){
            log.error("Kateqoriya silinərkən xəta baş verdi - 99: {}",e.getMessage());
            throw new RuntimeException("Kateqoriya silinmir! - 100");
        }
    }
}
