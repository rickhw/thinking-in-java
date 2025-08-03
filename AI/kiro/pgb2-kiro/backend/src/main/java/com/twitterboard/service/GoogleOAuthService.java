package com.twitterboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitterboard.config.OAuth2Config;
import com.twitterboard.dto.GoogleUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class GoogleOAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuthService.class);
    
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    
    @Autowired
    private OAuth2Config oAuth2Config;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public GoogleOAuthService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Exchange authorization code for access token and get user info
     * @param authorizationCode Authorization code from Google
     * @return Google user information
     * @throws Exception if OAuth process fails
     */
    public GoogleUserInfo getUserInfo(String authorizationCode) throws Exception {
        // Step 1: Exchange authorization code for access token
        String accessToken = exchangeCodeForToken(authorizationCode);
        
        // Step 2: Use access token to get user information
        return getUserInfoFromGoogle(accessToken);
    }
    
    /**
     * Exchange authorization code for access token
     * @param authorizationCode Authorization code
     * @return Access token
     * @throws Exception if token exchange fails
     */
    private String exchangeCodeForToken(String authorizationCode) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", oAuth2Config.getClientId());
        params.add("client_secret", oAuth2Config.getClientSecret());
        params.add("code", authorizationCode);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", "postmessage"); // For mobile/SPA apps
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                GOOGLE_TOKEN_URL, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String accessToken = jsonNode.get("access_token").asText();
                
                logger.info("Successfully exchanged authorization code for access token");
                return accessToken;
            } else {
                logger.error("Failed to exchange authorization code. Status: {}, Body: {}", 
                           response.getStatusCode(), response.getBody());
                throw new Exception("Failed to exchange authorization code for access token");
            }
        } catch (Exception e) {
            logger.error("Error exchanging authorization code for token", e);
            throw new Exception("Failed to exchange authorization code: " + e.getMessage());
        }
    }
    
    /**
     * Get user information from Google using access token
     * @param accessToken Google access token
     * @return Google user information
     * @throws Exception if user info retrieval fails
     */
    private GoogleUserInfo getUserInfoFromGoogle(String accessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                GOOGLE_USER_INFO_URL, HttpMethod.GET, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                GoogleUserInfo userInfo = new GoogleUserInfo();
                userInfo.setId(jsonNode.get("id").asText());
                userInfo.setEmail(jsonNode.get("email").asText());
                userInfo.setName(jsonNode.get("name").asText());
                userInfo.setPicture(jsonNode.has("picture") ? jsonNode.get("picture").asText() : null);
                userInfo.setVerifiedEmail(jsonNode.has("verified_email") ? jsonNode.get("verified_email").asBoolean() : false);
                
                logger.info("Successfully retrieved user info for Google ID: {}", userInfo.getId());
                return userInfo;
            } else {
                logger.error("Failed to get user info. Status: {}, Body: {}", 
                           response.getStatusCode(), response.getBody());
                throw new Exception("Failed to get user information from Google");
            }
        } catch (Exception e) {
            logger.error("Error getting user info from Google", e);
            throw new Exception("Failed to get user information: " + e.getMessage());
        }
    }
    
    /**
     * Validate Google ID token (alternative method for client-side authentication)
     * @param idToken Google ID token
     * @return Google user information
     * @throws Exception if token validation fails
     */
    public GoogleUserInfo validateIdToken(String idToken) throws Exception {
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                // Verify the token is for our application
                String audience = jsonNode.get("aud").asText();
                if (!oAuth2Config.getClientId().equals(audience)) {
                    throw new Exception("Invalid token audience");
                }
                
                GoogleUserInfo userInfo = new GoogleUserInfo();
                userInfo.setId(jsonNode.get("sub").asText());
                userInfo.setEmail(jsonNode.get("email").asText());
                userInfo.setName(jsonNode.get("name").asText());
                userInfo.setPicture(jsonNode.has("picture") ? jsonNode.get("picture").asText() : null);
                userInfo.setVerifiedEmail(jsonNode.has("email_verified") ? jsonNode.get("email_verified").asBoolean() : false);
                
                logger.info("Successfully validated ID token for Google ID: {}", userInfo.getId());
                return userInfo;
            } else {
                logger.error("Failed to validate ID token. Status: {}, Body: {}", 
                           response.getStatusCode(), response.getBody());
                throw new Exception("Failed to validate Google ID token");
            }
        } catch (Exception e) {
            logger.error("Error validating Google ID token", e);
            throw new Exception("Failed to validate ID token: " + e.getMessage());
        }
    }
}