package com.bookstore.gateway.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@RestController
@RequestMapping("/api")
public class GatewayController {

    @Value("${service.urls.catalog:http://catalog-service:8085}")
    private String catalogServiceUrl;

    @Value("${service.urls.author:http://author-service:8084}")
    private String authorServiceUrl;

    @Value("${service.urls.category:http://category-service:8083}")
    private String categoryServiceUrl;

    @Value("${service.urls.image:http://image-service:8082}")
    private String imageServiceUrl;

    private final RestTemplate restTemplate;

    public GatewayController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallback")
    @RequestMapping(value = "/catalog/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> catalogProxy(
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient client,
            HttpServletRequest request,
            @RequestBody(required = false) String body) {

        System.out.println("=== CATALOG PROXY CALLED ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Catalog Service URL: " + catalogServiceUrl);

        String path = request.getRequestURI().replace("/api/catalog", "/api/catalog");
        String queryString = request.getQueryString();
        String fullUrl = catalogServiceUrl + path + (queryString != null ? "?" + queryString : "");

        System.out.println("Full URL: " + fullUrl);
        System.out.println("=== END DEBUG ===");

        return proxyRequest(client, request, body, fullUrl);
    }

    @CircuitBreaker(name = "authorService", fallbackMethod = "fallback")
    @RequestMapping(value = "/authors/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> authorProxy(
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient client,
            HttpServletRequest request,
            @RequestBody(required = false) String body) {

        String path = request.getRequestURI().replace("/api/authors", "/api/authors");
        String queryString = request.getQueryString();
        String fullUrl = authorServiceUrl + path + (queryString != null ? "?" + queryString : "");

        return proxyRequest(client, request, body, fullUrl);
    }

    @CircuitBreaker(name = "categoryService", fallbackMethod = "fallback")
    @RequestMapping(value = "/categories/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> categoryProxy(
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient client,
            HttpServletRequest request,
            @RequestBody(required = false) String body) {

        String path = request.getRequestURI().replace("/api/categories", "/api/categories");
        String queryString = request.getQueryString();
        String fullUrl = categoryServiceUrl + path + (queryString != null ? "?" + queryString : "");

        return proxyRequest(client, request, body, fullUrl);
    }

    @CircuitBreaker(name = "imageService", fallbackMethod = "fallback")
    @RequestMapping(value = "/images/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> imageProxy(
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient client,
            HttpServletRequest request,
            @RequestBody(required = false) String body) {

        String path = request.getRequestURI().replace("/api/images", "/api/images");
        String queryString = request.getQueryString();
        String fullUrl = imageServiceUrl + path + (queryString != null ? "?" + queryString : "");

        return proxyRequest(client, request, body, fullUrl);
    }

    @GetMapping("/aggregate/{service}/v3/api-docs")
    public ResponseEntity<String> swaggerProxy(@PathVariable("service") String service) {
        String targetUrl;
        switch (service) {
            case "catalog-service":
                targetUrl = catalogServiceUrl;
                break;
            case "author-service":
                targetUrl = authorServiceUrl;
                break;
            case "category-service":
                targetUrl = categoryServiceUrl;
                break;
            case "image-service":
                targetUrl = imageServiceUrl;
                break;
            default:
                return ResponseEntity.badRequest().body("{\"error\":\"Invalid service name\"}");
        }

        try {
            String response = restTemplate.getForObject(targetUrl + "/v3/api-docs", String.class);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("{\"error\":\"Service unavailable: " + service + "\"}");
        }
    }

    private ResponseEntity<String> proxyRequest(OAuth2AuthorizedClient client, HttpServletRequest request, String body, String targetUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();

            // OAuth2 token əlavə et
            if (client != null && client.getAccessToken() != null) {
                headers.set("Authorization", "Bearer " + client.getAccessToken().getTokenValue());
            }

            // Content-Type header-ini kopyala
            String contentType = request.getContentType();
            if (contentType != null) {
                headers.set("Content-Type", contentType);
            }

            // Digər header-ləri kopyala
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!headerName.equalsIgnoreCase("authorization") &&
                        !headerName.equalsIgnoreCase("host") &&
                        !headerName.equalsIgnoreCase("content-length")) {
                    headers.set(headerName, request.getHeader(headerName));
                }
            }

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            ResponseEntity<String> response = restTemplate.exchange(targetUrl, method, entity, String.class);

            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (Exception e) {
            System.err.println("Proxy request failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    // Fallback method-ları
    public ResponseEntity<String> fallback(OAuth2AuthorizedClient client, HttpServletRequest request, String body, Throwable t) {
        System.err.println("Circuit breaker fallback triggered: " + t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":\"Service temporarily unavailable. Please try again later.\", \"message\":\"" + t.getMessage() + "\"}");
    }

    public ResponseEntity<String> fallback(String service, Throwable t) {
        System.err.println("Swagger fallback triggered for " + service + ": " + t.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":\"Swagger documentation temporarily unavailable for " + service + "\"}");
    }
}
