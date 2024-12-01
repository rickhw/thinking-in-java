package com.gtcafe.asimov.platform.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyAuthenticationManager implements AuthenticationManager {
    private final ApiKeyStore apiKeyStore;

    public ApiKeyAuthenticationManager(ApiKeyStore apiKeyStore) {
        this.apiKeyStore = apiKeyStore;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String apiKey = (String) authentication.getPrincipal();
        if (!apiKeyStore.isValidApiKey(apiKey)) {
            throw new BadCredentialsException("Invalid API Key");
        }
        authentication.setAuthenticated(true);
        return authentication;
    }
}