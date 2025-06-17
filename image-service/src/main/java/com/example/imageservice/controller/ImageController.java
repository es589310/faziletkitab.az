package com.example.imageservice.controller;

import com.example.imageservice.entity.Image;
import com.example.imageservice.exception.ImageNotFoundException;
import com.example.imageservice.response.ImageResponse;
import com.example.imageservice.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/{bookId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ImageResponse> getImagesByBookId(@PathVariable Long bookId){
        try {
            Image getImages = imageService.getImageByBookId(bookId);
            ImageResponse imageResponse = new ImageResponse(getImages.getImageUrl());
            return ResponseEntity.ok(imageResponse);
        }catch (ImageNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ImageResponse("This bookId does not exist"));
        }
    }

    @Operation(summary = "Upload an image for a book", description = "Upload a single image file for a book.")
    @PostMapping(value = "/upload-image/{bookId}", consumes = "multipart/form-data")
    public ResponseEntity<Image> uploadImage(@Parameter(description = "Book ID", required = true) @PathVariable Long bookId,
                                             @Parameter(description = "Image file", required = true) @RequestParam("imageFile") MultipartFile imageFile) throws IOException{
        try {
            Image uploadedImage = imageService.uploadImage(bookId, imageFile);
            return ResponseEntity.ok(uploadedImage);
        }catch (ImageNotFoundException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId){
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}
