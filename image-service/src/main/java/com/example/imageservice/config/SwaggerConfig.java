package com.example.imageservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Image Service API", version = "1.0", description = "Image Service Swagger"))
public class SwaggerConfig {
}
