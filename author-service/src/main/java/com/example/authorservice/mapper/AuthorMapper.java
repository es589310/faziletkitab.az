package com.example.authorservice.mapper;

import com.example.authorservice.entity.Author;
import com.example.authorservice.request.AuthorRequest;
import com.example.authorservice.response.AuthorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "birthDate", source = "birthDate")
    Author toEntity(AuthorRequest authorRequest);


    @Mapping(target = "birthDate",source = "birthDate")
    AuthorResponse toResponse(Author author);

}
