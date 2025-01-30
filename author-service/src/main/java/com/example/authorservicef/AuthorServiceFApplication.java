package com.example.authorservicef;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
public class AuthorServiceFApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorServiceFApplication.class, args);
    }

}
