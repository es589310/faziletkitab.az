package com.example.elasticsearchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.example.elasticsearchservice.repository")
public class ElasticsearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchServiceApplication.class, args);
    }

}