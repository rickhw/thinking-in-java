import com.messageboard.entity.User;

public class TestRunner {
    public static void main(String[] args) {
        try {
            User user = User.builder()
                    .ssoId("test123")
                    .username("testuser")
                    .email("test@example.com")
                    .build();
            
            System.out.println("User created successfully: " + user.getUsername());
            System.out.println("User is active: " + user.isActive());
            System.out.println("Effective display name: " + user.getEffectiveDisplayName());
            
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}