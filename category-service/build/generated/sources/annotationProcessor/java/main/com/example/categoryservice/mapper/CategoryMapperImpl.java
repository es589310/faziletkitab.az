package com.example.categoryservice.mapper;

import com.example.categoryservice.entity.Category;
import com.example.categoryservice.request.CategoryRequest;
import com.example.categoryservice.response.CategoryResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-18T01:21:37+0400",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.12.1.jar, environment: Java 17.0.15 (Amazon.com Inc.)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public Category toEntity(CategoryRequest categoryRequest) {
        if ( categoryRequest == null ) {
            return null;
        }

        Category category = new Category();

        category.setCategoryName( categoryRequest.getCategoryName() );
        category.setCategoryDescription( categoryRequest.getCategoryDescription() );

        return category;
    }

    @Override
    public CategoryResponse toResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse categoryResponse = new CategoryResponse();

        categoryResponse.setCategoryId( category.getCategoryId() );
        categoryResponse.setCategoryName( category.getCategoryName() );
        categoryResponse.setCategoryDescription( category.getCategoryDescription() );

        return categoryResponse;
    }
}
