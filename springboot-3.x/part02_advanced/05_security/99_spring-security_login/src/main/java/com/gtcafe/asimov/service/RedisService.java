
import com.example.auth.model.JwtSession;

public interface RedisService {
    void saveJwtSession(JwtSession jwtSession);
    JwtSession getJwtSession(String username);
    void deleteJwtSession(String username);
}