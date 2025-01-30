package com.example.authorservice.mapper;

import com.example.authorservice.entity.Author;
import com.example.authorservice.request.AuthorRequest;
import com.example.authorservice.response.AuthorResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-01-30T16:34:39+0400",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.12.1.jar, environment: Java 17.0.14 (Amazon.com Inc.)"
)
@Component
public class AuthorMapperImpl implements AuthorMapper {

    @Override
    public Author toEntity(AuthorRequest authorRequest) {
        if ( authorRequest == null ) {
            return null;
        }

        Author author = new Author();

        author.setBirthDate( authorRequest.birthDate() );
        author.setName( authorRequest.name() );
        author.setBiography( authorRequest.biography() );

        return author;
    }

    @Override
    public AuthorResponse toResponse(Author author) {
        if ( author == null ) {
            return null;
        }

        AuthorResponse authorResponse = new AuthorResponse();

        authorResponse.setBirthDate( author.getBirthDate() );
        authorResponse.setAuthorId( author.getAuthorId() );
        authorResponse.setName( author.getName() );
        authorResponse.setBiography( author.getBiography() );

        return authorResponse;
    }
}
