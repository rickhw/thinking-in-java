
import com.example.auth.model.JwtSession;
import com.example.auth.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RedisService redisService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisService = redisService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = jwtTokenProvider.getTokenFromRequest(request);
            
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // Get username from token
                String username = jwtTokenProvider.getUsernameFromToken(token);
                
                // Check if session exists in Redis
                JwtSession jwtSession = redisService.getJwtSession(username);
                
                if (jwtSession != null && jwtSession.getToken().equals(token)) {
                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    logger.info("User session not found in Redis or token mismatch");
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}