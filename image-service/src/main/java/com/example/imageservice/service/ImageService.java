package com.example.imageservice.service;

import com.example.imageservice.entity.Image;
import com.example.imageservice.exception.ImageNotFoundException;
import com.example.imageservice.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final ImageRepository imageRepository;
    private final String uploadDirectory = "/home/mrdoc/IdeaProjects/faziletkitab.az/imageUpload";

    public Image uploadImage(Long bookId, MultipartFile imageFile) throws IOException {
        String fileName = imageFile.getOriginalFilename(); //şəklin adını aldım
        String filePath = uploadDirectory + File.separator + fileName; //şəklin linkini aldım

        File destinationFile = new File(filePath);
        imageFile.transferTo(destinationFile); // faylı müəyyən linkə qeyd etdim

        Image images = new Image(); //database-ə qeyd üçün İmage-ın obyektini yaratdım
        images.setBookId(bookId);
        images.setImageUrl(filePath);
        images.setUploadedAt(LocalDateTime.now());

        return imageRepository.save(images);
    }

    public Image getImagesByBookId(Long bookId) {
        return imageRepository.findByBookId(bookId);
    }

    public void deleteImage(Long imageId){
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Şəkil tapılmadı!"));
        new File(image.getImageUrl()).delete();
        imageRepository.delete(image);
    }

}
