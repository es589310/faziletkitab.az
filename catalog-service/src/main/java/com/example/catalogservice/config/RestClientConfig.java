package com.example.catalogservice.config;

import com.example.catalogservice.client.AuthorClient;
import com.example.catalogservice.client.CategoryClient;
import com.example.catalogservice.client.ImageClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
@Slf4j
public class RestClientConfig {

    @Value("${authors.service.url}")
    private String authorServiceUrl;

    @Value("${categories.service.url}")
    private String categoryServiceUrl;

    @Value("${image.service.url}")
    private String imageServiceUrl;

    @PostConstruct
    public void logProperties() {
        log.info("author.service.url: {}", authorServiceUrl);
        log.info("categories.service.url: {}", categoryServiceUrl);
        log.info("image.service.url: {}", imageServiceUrl);
    }

    @Bean
    public AuthorClient authorClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(authorServiceUrl)
                .requestFactory(getClientRequestFactory())
                .build();

        var restClientAdapter = RestClientAdapter.create(restClient);
        var httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return httpServiceProxyFactory.createClient(AuthorClient.class);
    }

    @Bean
    public CategoryClient categoryClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(categoryServiceUrl)
                .requestFactory(getClientRequestFactory())
                .build();

        var restClientAdapter = RestClientAdapter.create(restClient);
        var httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return httpServiceProxyFactory.createClient(CategoryClient.class);
    }

    @Bean
    public ImageClient imageClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(imageServiceUrl)
                .requestFactory(getClientRequestFactory())
                .build();

        var restClientAdapter = RestClientAdapter.create(restClient);
        var httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return httpServiceProxyFactory.createClient(ImageClient.class);
    }

    private ClientHttpRequestFactory getClientRequestFactory() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(3))
                .withReadTimeout(Duration.ofSeconds(3));

        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }
}
