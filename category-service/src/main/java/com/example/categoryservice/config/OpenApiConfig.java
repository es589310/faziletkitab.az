package com.example.categoryservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI authorServiceAPI(){
        return new OpenAPI()
                .info(new Info().title("Category Service API")
                        .description("Category halında ümumi qeyd edən servis API")
                        .version("v.0.0.1")
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Siz Category ilə hansı kateqoriyaların olduğunu Wiki sənədlərinə müraciətdə baxa bilərsiniz bilərsiniz")
                        .url("https://category-service-dummy-url.com/docs"));
    }

}
