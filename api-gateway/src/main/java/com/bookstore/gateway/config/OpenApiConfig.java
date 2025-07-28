package com.bookstore.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Bookstore API Gateway")
                        .version("1.0.0")
                        .description("API Gateway for E-Bookstore Microservices")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@bookstore.com")))
                // SERVER URL ƏLAVƏ EDİN
                .servers(List.of(
                        new Server().url("http://localhost:9000").description("API Gateway")
                ))
                .addSecurityItem(new SecurityRequirement().addList("oauth2"))
                .components(new Components()
                        .addSecuritySchemes("oauth2",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                        .authorizationUrl("http://localhost:8181/realms/e-bookstore/protocol/openid-connect/auth")
                                                        .tokenUrl("http://localhost:8181/realms/e-bookstore/protocol/openid-connect/token")
                                                        .scopes(new Scopes()
                                                                .addString("openid", "OpenID Connect scope")
                                                                .addString("profile", "User profile scope")
                                                                .addString("email", "User email scope")
                                                        )
                                                )
                                        )
                        ));
    }
}