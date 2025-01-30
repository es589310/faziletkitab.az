package com.example.catalogservice.config;

import com.example.catalogservice.client.AuthorClient;
import com.example.catalogservice.client.CategoryClient;
import com.example.catalogservice.client.ImageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Value("${authors.service.url}")
    private String authorServiceUrl;

    @Value("${categories.service.url}")
    private String categoryServiceUrl;

    @Value("${image.service.url}")
    private String imageServiceUrl;

    @Bean
    public AuthorClient authorClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(authorServiceUrl).build();

        var restClientAdapter = RestClientAdapter.create(restClient);
        var httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return httpServiceProxyFactory.createClient(AuthorClient.class);
    }

    @Bean
    public CategoryClient categoryClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(categoryServiceUrl).build();

        var restClientAdapter = RestClientAdapter.create(restClient);
        var httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return httpServiceProxyFactory.createClient(CategoryClient.class);
    }

    @Bean
    public ImageClient imageClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(imageServiceUrl).build();

        var restClientAdapter = RestClientAdapter.create(restClient);
        var httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return httpServiceProxyFactory.createClient(ImageClient.class);
    }

}
