package com.example.elasticsearchservice.config;

import com.example.elasticsearchservice.client.CategoryClient;
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
public class RestClientConfig {

    @Value("${categories.service.url}")
    private String categoryServiceUrl;

    @Bean
    public CategoryClient categoryClient() {
        org.springframework.web.client.RestClient restClient = RestClient.builder()
                .baseUrl(categoryServiceUrl)
                .requestFactory(getClientRequestFactory())
                .build();

        var restClientAdapter = RestClientAdapter.create(restClient);
        var httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return httpServiceProxyFactory.createClient(CategoryClient.class);
    }

    private ClientHttpRequestFactory getClientRequestFactory() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(3))
                .withReadTimeout(Duration.ofSeconds(3));

        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }
}
