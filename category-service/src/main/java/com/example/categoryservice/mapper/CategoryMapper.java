package com.example.categoryservice.mapper;

import com.example.categoryservice.entity.Category;
import com.example.categoryservice.request.CategoryRequest;
import com.example.categoryservice.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    //CategoryRequest-dən Category-yə
    @Mapping(target = "categoryId", ignore = true)
    @Mapping(target = "categoryName", source = "categoryName")
    @Mapping(target = "categoryDescription", source = "categoryDescription")
    Category toEntity(CategoryRequest categoryRequest);


    //Category-dən CategoryResponse-a
    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "categoryName", source = "categoryName")
    @Mapping(target = "categoryDescription", source = "categoryDescription")
    CategoryResponse toResponse(Category category);

    //CategoryRequest-dən Category-yə - update zamanı istənilən dəyişim üçün buradan dəyişiklik edə bilərsən
//    @Mapping(target = "categoryId", source = "categoryId")
//    @Mapping(target = "categoryName", source = "categoryName")
//    @Mapping(target = "categoryDescription", source = "categoryDescription")
//    CategoryResponse toEntityForUpdate(Category category);


}
