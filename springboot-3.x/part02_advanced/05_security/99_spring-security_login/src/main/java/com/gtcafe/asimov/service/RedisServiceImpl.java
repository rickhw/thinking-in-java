
import com.example.auth.model.JwtSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "jwt_session:";

    public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveJwtSession(JwtSession jwtSession) {
        String key = KEY_PREFIX + jwtSession.getUsername();
        long timeToLive = (jwtSession.getExpirationTime() - System.currentTimeMillis()) / 1000;
        
        redisTemplate.opsForValue().set(key, jwtSession, timeToLive, TimeUnit.SECONDS);
    }

    @Override
    public JwtSession getJwtSession(String username) {
        String key = KEY_PREFIX + username;
        return (JwtSession) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deleteJwtSession(String username) {
        String key = KEY_PREFIX + username;
        redisTemplate.delete(key);
    }
}