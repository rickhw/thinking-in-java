package com.gtcafe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.Map;

@Service
public class ApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestTemplateConfig restTemplateConfig;

    @Value("${app.api.base-url}")
    private String baseUrl;

    public ResponseEntity<String> getUser(Map<String, String> queryParams) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/users")
                .queryParam("userId", queryParams.get("userId"))
                .build()
                .toUri();

        HttpHeaders headers = restTemplateConfig.createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return handleResponse(restTemplate.exchange(uri, HttpMethod.GET, entity, String.class));
    }

    public ResponseEntity<String> updateUser(Map<String, Object> payload) {
        URI uri = URI.create(baseUrl + "/users/update");

        HttpHeaders headers = restTemplateConfig.createHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        return handleResponse(restTemplate.exchange(uri, HttpMethod.POST, entity, String.class));
    }

    private ResponseEntity<String> handleResponse(ResponseEntity<String> response) {
        HttpStatus status = response.getStatusCode();
        // HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());
        if (status.is2xxSuccessful()) {
            System.out.println("Success: " + response.getBody());
        } else if (status.is3xxRedirection()) {
            System.out.println("Redirection: " + response.getHeaders().getLocation());
        } else if (status.is4xxClientError()) {
            System.out.println("Client Error: " + status);
        } else if (status.is5xxServerError()) {
            System.out.println("Server Error: " + status);
        }
        return response;
    }
}
