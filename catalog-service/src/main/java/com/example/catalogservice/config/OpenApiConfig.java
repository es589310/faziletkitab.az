package com.example.catalogservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogServiceAPI(){
        return new OpenAPI()
                .info(new Info().title("Catalog Service API")
                        .description("Catalog halında ümumi qeyd edən servis API")
                        .version("v.0.0.1")
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Siz Catalog üzrə məhsul yığma xidmətinin Wiki Sənədlərinə müraciət edə bilərsiniz")
                        .url("http://catalogs-service-dummy-url.com/docs"));
    }
}
