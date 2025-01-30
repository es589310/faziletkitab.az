package com.example.authorservicef.mapper;

import com.example.authorservicef.entity.Author;
import com.example.authorservicef.request.AuthorRequest;
import com.example.authorservicef.response.AuthorResponse;
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
