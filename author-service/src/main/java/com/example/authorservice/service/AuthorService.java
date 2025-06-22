package com.example.authorservice.service;

import com.example.authorservice.entity.Author;
import com.example.authorservice.exception.AuthorNotFoundException;
import com.example.authorservice.mapper.AuthorMapper;
import com.example.authorservice.repository.AuthorRepository;
import com.example.authorservice.request.AuthorRequest;
import com.example.authorservice.response.AuthorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorService {
    private final AuthorRepository repository;
    private final AuthorMapper authorMapper;

    public AuthorResponse addAuthor(AuthorRequest authorRequest){
        try {
            log.info("Yazar məlumatları set olunacaq!");
            Author newAuthor = authorMapper.toEntity(authorRequest);

            Author savedAuthor = repository.save(newAuthor);

            return authorMapper.toResponse(savedAuthor);
        }catch (AuthorNotFoundException e){
            log.error("Servis/Sətr 36 : {}", e.getMessage());
            throw new RuntimeException("məlumatlar yaranmadı!");
        }
    }



    public List<AuthorResponse> getAllAuthor(){
        try {
            log.info("Məlumatların gətirilməsi üçün iş gedir (sətr 44)");
            return repository.findAll().stream()

                        .map(author -> new AuthorResponse(
                                author.getAuthorId(),
                                author.getName(),
                                author.getBiography(),
                                author.getBirthDate()
                        ))
                        .toList();
        }catch (AuthorNotFoundException e){
            log.error("Məlumatları gətirdiyimiz zaman error baş verdi! /Servis/Sətr 50: {}", e.getMessage());
            throw new RuntimeException("Məlumatların hamısı gəlmədi!");
        }

    }



    public AuthorResponse getOneAuthor(Long authorId){
        try {
            Author author = repository.findById(authorId)
                            .orElseThrow(() -> new RuntimeException("Author tapılmır! 63"));
            log.info("Yazar id-ə görə çağırılmağa hazırdır!");
            return new AuthorResponse(author.getAuthorId(), author.getName(),author.getBiography(),author.getBirthDate());
        }catch (AuthorNotFoundException e){
            log.error("Yazarı çağırarkən xəta oldu : {}",e.getMessage());
            throw new RuntimeException("Yazar id-ə görə çağırıla bilinmir!");
        }
    }



    public AuthorResponse updateAuthorInfo(Long authorId, AuthorRequest authorRequest){
        try {
            Author upadetingAuthor = repository.findByAuthorId(authorId);
            log.info("Yazar məlumatları yenilənmək üçün baxışdadır!");
            upadetingAuthor.setName(authorRequest.name());
            upadetingAuthor.setBiography(authorRequest.biography());
            upadetingAuthor.setBirthDate(authorRequest.birthDate());

            Author savedAuthor = repository.save(upadetingAuthor);
            log.info("Yazar məlumatları yeniləndi");
            return authorMapper.toResponse(savedAuthor);

        }catch (AuthorNotFoundException e){
            log.error("Yazar məlumatları yenilənmədi! : {}", e.getMessage());
            throw new RuntimeException("Yazar məlumatları update olmur" );
        }
    }



    public void deleteAuthor(Long authorId){
        try {
            Author findAuthor = repository.findByAuthorId(authorId);
            log.info("Silinmək üçün yazar tapıldı");
            repository.delete(findAuthor);
            log.info("Yazar silindi");
        }catch (AuthorNotFoundException e){
            log.error("Yazar silərkən xəta baş verdi: {} ", e.getMessage());
            throw new RuntimeException("Silməyə yazar tapılmadı");
        }
    }

}
