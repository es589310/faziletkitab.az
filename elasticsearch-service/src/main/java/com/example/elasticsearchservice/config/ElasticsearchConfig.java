package com.example.elasticsearchservice.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    public RestClient customElasticsearchClient() {
        // Kimlik doğrulama sağlayıcısı oluştur
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        // HttpHost'ları oluştur
        HttpHost[] httpHosts = Arrays.stream(elasticsearchUris.split(","))
                .map(uri -> {
                    String[] parts = uri.split("://");
                    String protocol = parts[0]; // http veya https
                    String[] hostAndPort = parts[1].split(":");
                    String host = hostAndPort[0];
                    int port = Integer.parseInt(hostAndPort[1]);
                    return new HttpHost(host, port, protocol);
                })
                .toArray(HttpHost[]::new);

        // RestClient'ı oluştur ve kimlik doğrulama bilgilerini ekle
        return RestClient.builder(httpHosts)
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider))
                .build();
    }
}