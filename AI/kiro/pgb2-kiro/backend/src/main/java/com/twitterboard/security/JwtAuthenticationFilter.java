package com.twitterboard.security;

import com.twitterboard.entity.User;
import com.twitterboard.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                if (!tokenProvider.isAccessToken(jwt)) {
                    logger.warn("Invalid token type for authentication");
                    filterChain.doFilter(request, response);
                    return;
                }
                
                if (tokenProvider.isTokenExpired(jwt)) {
                    logger.warn("Token is expired");
                    filterChain.doFilter(request, response);
                    return;
                }
                
                Long userId = tokenProvider.getUserIdFromToken(jwt);
                
                Optional<User> userOptional = userRepository.findById(userId);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    UserDetails userDetails = createUserDetails(user);
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // Add user info to request attributes for easy access
                    request.setAttribute("currentUser", user);
                    request.setAttribute("currentUserId", userId);
                } else {
                    logger.warn("User not found for token: {}", userId);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract JWT token from request
     * @param request HTTP request
     * @return JWT token or null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return tokenProvider.extractTokenFromHeader(bearerToken);
    }
    
    /**
     * Create UserDetails from User entity
     * @param user User entity
     * @return UserDetails
     */
    private UserDetails createUserDetails(User user) {
        return new CustomUserPrincipal(user);
    }
}