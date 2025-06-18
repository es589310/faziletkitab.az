package com.bookstore.gateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.*;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
public class Routes {

    @Value("${service.urls.catalog}")
    private String catalogServiceUrl;

    @Value("${service.urls.author}")
    private String authorServiceUrl;

    @Value("${service.urls.category}")
    private String categoryServiceUrl;

    @Value("${service.urls.image}")
    private String imageServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> catalogServiceRoute() {
        return GatewayRouterFunctions.route("catalog-service")
                .route(RequestPredicates.path("/api/catalog"), HandlerFunctions.http(catalogServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("catalogServiceCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> catalogServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("catalog-service-swagger")
                .route(RequestPredicates.path("/aggregate/catalog-service/v3/api-docs"), HandlerFunctions.http(catalogServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("catalogServiceSwaggerCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> authorServiceRoute() {
        return GatewayRouterFunctions.route("author-service")
                .route(RequestPredicates.path("/api/authors"), HandlerFunctions.http(authorServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("authorServiceCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> authorServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("author-service-swagger")
                .route(RequestPredicates.path("/aggregate/author-service/v3/api-docs"), HandlerFunctions.http(authorServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("authorServiceSwaggerCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> categoryServiceRoute() {
        return GatewayRouterFunctions.route("category-service")
                .route(RequestPredicates.path("/api/categories"), HandlerFunctions.http(categoryServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("categoryServiceCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> categoryServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("category-service-swagger")
                .route(RequestPredicates.path("/aggregate/category-service/v3/api-docs"), HandlerFunctions.http(categoryServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("categoryServiceSwaggerCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> imageServiceRoute() {
        return GatewayRouterFunctions.route("image-service")
                .route(RequestPredicates.path("/api/images"), HandlerFunctions.http(imageServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("imageServiceCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> imageServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("image-service-swagger")
                .route(RequestPredicates.path("/aggregate/image-service/v3/api-docs"), HandlerFunctions.http(imageServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("imageServiceSwaggerCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return route("fallbackRoute")
                .GET("/fallbackRoute", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Service Unavailable, please try again later"))
                .build();
    }
}