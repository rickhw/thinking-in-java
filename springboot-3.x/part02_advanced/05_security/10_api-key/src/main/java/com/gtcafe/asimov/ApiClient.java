package com.gtcafe.asimov;


import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ApiClient {
    private final WebClient webClient;

    public ApiClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultHeader("X-API-KEY", "service1-api-key")
                .build();
    }

    public String callInternalHealth() {
        return webClient.get()
                .uri("/internal/health")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String callPublicVersion() {
        return webClient.get()
                .uri("/public/version")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}