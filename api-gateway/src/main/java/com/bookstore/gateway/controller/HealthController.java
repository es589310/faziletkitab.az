package com.bookstore.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Value("${service.urls.catalog}")
    private String catalogServiceUrl;

    @Value("${service.urls.author}")
    private String authorServiceUrl;

    @Value("${service.urls.category}")
    private String categoryServiceUrl;

    @Value("${service.urls.image}")
    private String imageServiceUrl;

    private final RestTemplate restTemplate;

    public HealthController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/services")
    public ResponseEntity<Map<String, String>> checkServicesHealth() {
        Map<String, String> healthStatus = new HashMap<>();

        // Check Catalog Service
        healthStatus.put("catalog-service", checkServiceHealth(catalogServiceUrl));
        healthStatus.put("author-service", checkServiceHealth(authorServiceUrl));
        healthStatus.put("category-service", checkServiceHealth(categoryServiceUrl));
        healthStatus.put("image-service", checkServiceHealth(imageServiceUrl));

        return ResponseEntity.ok(healthStatus);
    }

    private String checkServiceHealth(String serviceUrl) {
        try {
            restTemplate.getForObject(serviceUrl + "/actuator/health", String.class);
            return "UP";
        } catch (Exception e) {
            return "DOWN - " + e.getMessage();
        }
    }
}
