package com.example.catalogservice.client;

import com.example.catalogservice.response.ImageResponse;
import groovy.util.logging.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@Slf4j
public interface ImageClient {

    Logger log = LoggerFactory.getLogger(ImageClient.class);

    @GetExchange("/api/images/{bookId}")
    @CircuitBreaker(name = "image", fallbackMethod = "fallbackMethod")
    @Retry(name = "image")
    ImageResponse getImageByBookId(@PathVariable("bookId") Long bookId);

    default ImageResponse fallbackMethod(Long imageId, Throwable throwable) {
        log.warn("Cannot get image for imageId {}, failure reason: {}", imageId, throwable.getMessage());
        return new ImageResponse("https://fazilet-image-bucket.s3.us-east-1.amazonaws.com/default-image.jpg");
    }
}
