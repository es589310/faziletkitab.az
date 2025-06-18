package com.example.imageservice.service;

import com.example.imageservice.entity.Image;
import com.example.imageservice.exception.ImageNotFoundException;
import com.example.imageservice.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};

    public Image uploadImage(Long bookId, MultipartFile imageFile) throws IOException {
        imageRepository.findByBookId(bookId).ifPresent(image -> {
            throw new IllegalStateException("Image already exists for bookId: " + bookId);
        });

        if (imageFile == null || imageFile.isEmpty()) {
            throw new ImageNotFoundException("Image file cannot be empty");
        }
        if (imageFile.getSize() > MAX_FILE_SIZE) {
            throw new ImageNotFoundException("Image file cannot exceed 10MB");
        }
        String originalFilename = imageFile.getOriginalFilename();
        if (originalFilename == null || !isValidExtension(originalFilename)) {
            throw new ImageNotFoundException("Image file must be one of 'jpg', 'jpeg', 'png', 'gif': " + originalFilename);
        }

        // Unikal fayl adı
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID() + fileExtension;

        // S3-ə yüklə
        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .contentType(imageFile.getContentType())
                    .build(), RequestBody.fromInputStream(imageFile.getInputStream(), imageFile.getSize()));
            log.info("Successfully uploaded image to S3: {}", uniqueFileName);
        } catch (Exception e) {
            log.error("Failed to upload image to S3: {}", e.getMessage());
            throw new IOException("Failed to upload image to S3: " + e.getMessage(), e);
        }

        String imageUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileName)
                .build()).toExternalForm();
        if (imageUrl == null) {
            throw new IOException("Failed to generate S3 URL for file: " + uniqueFileName);
        }

        Image image = new Image();
        image.setBookId(bookId);
        image.setImageUrl(imageUrl);
        image.setUploadedAt(LocalDateTime.now());

        return imageRepository.save(image);
    }

    public Image getImageByBookId(Long bookId) {
        return imageRepository.findByBookId(bookId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found for bookId: " + bookId));
    }

    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found! ID: " + imageId));

        // S3-dən faylı sil
        try {
            String fileName = extractFileNameFromUrl(image.getImageUrl());
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build());
            log.info("Successfully deleted image from S3: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to delete image from S3: {}", e.getMessage());
        }

        // Verilənlər bazasından sil
        imageRepository.delete(image);
        log.info("Successfully deleted image from database: {}", imageId);
    }

    private boolean isValidExtension(String fileName) {
        for (String extension : ALLOWED_EXTENSIONS) {
            if (fileName.toLowerCase().endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    private String extractFileNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new IllegalArgumentException("Image URL is null or empty");
        }
        // S3 URL-dən fayl adını çıxar (məsələn: https://fazilet-image-bucket.s3.us-east-1.amazonaws.com/123-uuid.jpg -> 123-uuid.jpg)
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }
}