package com.bookstore.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Frontend URL-ləri əlavə edin
        config.addAllowedOrigin("http://localhost:9000");
        config.addAllowedOrigin("http://localhost:3000"); // React frontend üçün
        config.addAllowedOrigin("http://localhost:8080"); // Digər frontend üçün

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        // Swagger və API endpoint-ləri üçün
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
