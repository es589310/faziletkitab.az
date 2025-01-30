package com.example.authorservice.mapper;

import com.example.authorservice.entity.Author;
import com.example.authorservice.request.AuthorRequest;
import com.example.authorservice.response.AuthorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    //AuthorRequest-dən Author-a
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "birthDate", source = "birthDate")
    Author toEntity(AuthorRequest authorRequest);

    //Author-dan AuthorResponse-a
    @Mapping(target = "birthDate",source = "birthDate")
    AuthorResponse toResponse(Author author);
}
