
import com.example.auth.model.JwtSession;
import com.example.auth.security.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    public AuthServiceImpl(JwtTokenProvider jwtTokenProvider, RedisService redisService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisService = redisService;
    }

    @Override
    public void logout(String username) {
        redisService.deleteJwtSession(username);
    }

    @Override
    public JwtSession createJwtSession(Authentication authentication) {
        String token = jwtTokenProvider.generateToken(authentication);
        JwtSession jwtSession = jwtTokenProvider.getJwtSessionFromToken(token);
        
        // Store in Redis
        redisService.saveJwtSession(jwtSession);
        
        return jwtSession;
    }
}