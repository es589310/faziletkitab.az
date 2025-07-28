package com.bookstore.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                //  .setConnectTimeout(Duration.ofSeconds(10))
                //.setReadTimeout(Duration.ofSeconds(30))
                .requestFactory(() -> {
                    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
                    factory.setConnectTimeout(10000);
                    factory.setReadTimeout(30000);
                    return factory;
                })
                .build();
    }
}
