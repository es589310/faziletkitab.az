package com.example.catalogservice.client;


import com.example.catalogservice.response.ImageResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface ImageClient {

    @GetExchange("/api/images/{imageId}")
    ImageResponse getImageId(@PathVariable("imageId") Long imageId);

}
