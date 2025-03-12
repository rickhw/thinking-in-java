
import java.io.Serializable;
import java.util.List;

public class JwtSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private List<String> roles;
    private String token;
    private long expirationTime;

    public JwtSession() {
    }

    public JwtSession(String username, List<String> roles, String token, long expirationTime) {
        this.username = username;
        this.roles = roles;
        this.token = token;
        this.expirationTime = expirationTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
}