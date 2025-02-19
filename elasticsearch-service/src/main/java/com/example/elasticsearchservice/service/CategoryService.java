package com.example.elasticsearchservice.service;

import com.example.elasticsearchservice.client.CategoryClient;
import com.example.elasticsearchservice.elastic.CategoryElastic;
import com.example.elasticsearchservice.entity.CategoryEntity;
import com.example.elasticsearchservice.repository.CategoryElasticsearchRepo;
import com.example.elasticsearchservice.repository.CategoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryClient categoryClient;
    private final CategoryRepo categoryRepo;
    private final CategoryElasticsearchRepo categoryElasticsearchRepo;

    public void saveCategory(List<CategoryEntity> categoryEntities) {
        List<CategoryEntity> categories = categoryClient.getAllCategories();

        List<CategoryElastic> categoryElastics = categories.stream()
                .map(c -> new CategoryElastic(
                        c.getCategoryId().toString(),
                        c.getCategoryName(),
                        c.getCategoryDescription()
                ))
                .collect(Collectors.toList());

        categoryElasticsearchRepo.saveAll(categoryElastics);

        categoryRepo.saveAll(categoryEntities);
    }

    public List<CategoryElastic> searchCategories(String query) {
        return categoryElasticsearchRepo.findByCategoryNameContainingIgnoreCase(query);
    }


}


//    public CategoryEntity saveCategoty(CategoryEntity categoryEntity) {
//        CategoryEntity categories = categoryClient.getAllCategories(); // API'den verileri çekiyoruz
//
//
//        CategoryElastic indexList = categories.stream()
//                .map(c -> new CategoryElastic(
//                        c.getCategoryId().toString(),
//                        c.getCategoryName(),
//                        c.getCategoryDescription()
//                ))
//                .collect(Collectors.toList());
//
//        return categoryRepo.saveAll(indexList);
//    }
//
//    public List<CategoryElastic> searchCategories(String query) {
//        return categoryElasticsearchRepo.findByCategoryNameContainingIgnoreCase(query);
//    }
