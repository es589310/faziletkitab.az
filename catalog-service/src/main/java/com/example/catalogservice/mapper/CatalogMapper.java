package com.example.catalogservice.mapper;

import com.example.catalogservice.dto.CategoryDTO;
import com.example.catalogservice.entity.Catalog;
import com.example.catalogservice.request.CatalogRequest;
import com.example.catalogservice.response.CatalogResponse;
import com.example.catalogservice.dto.AuthorDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    @Mapping(source = "publishedDate", target = "publishedDate")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "title", target = "title")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "imageUrl", ignore = true)
    Catalog toEntity(CatalogRequest catalogRequest);

    @Mapping(source = "imageUrl", target = "imageUrl")
    @Mapping(source = "publishedDate", target = "publishedDate")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "title", target = "title")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryIdToDTO")
    @Mapping(target = "author", source = "authorId", qualifiedByName = "authorIdToDTO")
    CatalogResponse toResponse(Catalog catalog);

    @Named("authorIdToDTO")
    default AuthorDTO authorIdToDTO(Long authorId) {
        return new AuthorDTO(authorId, "Ad yoxdur", "Bioqrafiya yoxdur", "Tarix yoxdur");
    }

    @Named("categoryIdToDTO")
    default CategoryDTO categoryIdToDTO(Long categoryId){
        return new CategoryDTO(categoryId, "Kateqoriya adı yoxdur", "Kateqoriya açıqlaması yoxdur");
    }
}
