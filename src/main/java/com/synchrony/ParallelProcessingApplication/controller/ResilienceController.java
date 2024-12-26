package com.synchrony.ParallelProcessingApplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@RestController
@RequestMapping("/resilience")
public class ResilienceController {
    @GetMapping("/fallback")
    @CircuitBreaker(name = "default", fallbackMethod = "fallback")
    public String riskyEndpoint() {
        throw new RuntimeException("Simulated failure");
    }

    public String fallback(Throwable t) {
        return "Fallback response";
    }
}
