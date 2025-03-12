
import com.example.auth.model.JwtSession;
import org.springframework.security.core.Authentication;

public interface AuthService {
    void logout(String username);
    JwtSession createJwtSession(Authentication authentication);
}