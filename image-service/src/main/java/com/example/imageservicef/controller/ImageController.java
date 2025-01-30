package com.example.imageservicef.controller;

import com.example.imageservicef.entity.Image;
import com.example.imageservicef.exception.ImageNotFoundException;
import com.example.imageservicef.service.ImageService;
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

    @GetMapping("/{bookId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Image> getImagesByBookId(@PathVariable Long bookId){
        try {
            Image getImages = imageService.getImagesByBookId(bookId);
            return ResponseEntity.ok(getImages);
        }catch (ImageNotFoundException e){
            throw new ImageNotFoundException("Şəkil tapılmadı!");
        }
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId){
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }
}
