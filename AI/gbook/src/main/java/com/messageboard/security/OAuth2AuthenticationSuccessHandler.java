package com.messageboard.security;

import com.messageboard.entity.User;
import com.messageboard.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 authentication success handler that generates JWT tokens
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtUtil jwtUtil;
    
    @Value("${app.oauth2.redirect-uri:http://localhost:3000/auth/callback}")
    private String redirectUri;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to {}", redirectUri);
            return;
        }
        
        try {
            CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
            User localUser = oauth2User.getLocalUser();
            
            // Generate JWT tokens
            String accessToken = jwtUtil.generateAccessToken(localUser.getId(), localUser.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(localUser.getId(), localUser.getUsername());
            
            log.info("OAuth2 authentication successful for user: {}", localUser.getUsername());
            
            // Build redirect URL with tokens
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("access_token", accessToken)
                    .queryParam("refresh_token", refreshToken)
                    .queryParam("user_id", localUser.getId())
                    .queryParam("username", localUser.getUsername())
                    .build().toUriString();
            
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception ex) {
            log.error("Error handling OAuth2 authentication success", ex);
            
            // Redirect to error page
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "authentication_failed")
                    .queryParam("message", "Failed to process authentication")
                    .build().toUriString();
            
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}