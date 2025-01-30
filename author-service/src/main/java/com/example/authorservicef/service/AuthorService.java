package com.example.authorservicef.service;

import com.example.authorservicef.entity.Author;
import com.example.authorservicef.exception.AuthorNotFoundException;
import com.example.authorservicef.mapper.AuthorMapper;
import com.example.authorservicef.repository.AuthorRepository;
import com.example.authorservicef.request.AuthorRequest;
import com.example.authorservicef.response.AuthorResponse;
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
//            if (bookId == null){
//                throw new IllegalArgumentException("Kitab id-si yoxdur! (Sətr 23/Servis");
//            }
            log.info("Yazar məlumatları set olunacaq!");
            Author newAuthor = authorMapper.toEntity(authorRequest);

            log.error("Servis/Sətr 30");
            Author savedAuthor = repository.save(newAuthor);

            log.error("Servis/Sətr 33");
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
