package com.example.elasticsearchservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerConfig {

    @Bean("authorServiceCircuitBreakerConfig")
    public io.github.resilience4j.circuitbreaker.CircuitBreakerConfig authorServiceCircuitBreakerConfig() {
        return io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
            .slidingWindowType(SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(10)
            .failureRateThreshold(50)
            .waitDurationInOpenState(java.time.Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(5)
            .build();
    }
} 