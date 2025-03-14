package com.example.authorservice.controller;

import com.example.authorservice.request.AuthorRequest;
import com.example.authorservice.response.AuthorResponse;
import com.example.authorservice.service.AuthorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authors")
@Slf4j
public class AuthorController {
    private final AuthorService authorService;

    @PostMapping()
    public ResponseEntity<AuthorResponse> addAuthor(@RequestBody AuthorRequest authorRequest){
        try {
            AuthorResponse authorResponse = authorService.addAuthor(authorRequest);
            return new ResponseEntity<>(authorResponse, HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAuthors")
    public ResponseEntity<List<AuthorResponse>> getAllAuthor(){
        try {
            List<AuthorResponse> authorResponses = authorService.getAllAuthor();
            return new ResponseEntity<>(authorResponses, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{authorId}")
    public ResponseEntity<AuthorResponse> getOneAuthor(@PathVariable Long authorId){
        try {
            AuthorResponse getAuthorId = authorService.getOneAuthor(authorId);
            return new ResponseEntity<>(getAuthorId, HttpStatus.OK);
        }catch (Exception e){
            log.error("Yazar datası alınmadı! Yazar ID: {}", authorId, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{authorId}")
    public ResponseEntity<AuthorResponse> updateAuthorInfo(@PathVariable Long authorId, @RequestBody AuthorRequest authorRequest){
        try {
            AuthorResponse authorResponse = authorService.updateAuthorInfo(authorId,authorRequest);
            return new ResponseEntity<>(authorResponse, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long authorId){
        try {
            authorService.deleteAuthor(authorId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (Exception e){
            return new ResponseEntity<>(null,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


}
