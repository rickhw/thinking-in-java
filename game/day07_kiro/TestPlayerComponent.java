import rpg.Config;
import rpg.GamePanel;
import rpg.KeyHandler;
import rpg.components.*;
import rpg.entity.Direction;
import rpg.entity.Player;
import rpg.engine.Entity;

/**
 * Unit test for the component-based Player entity.
 * Tests player functionality, component integration, and legacy compatibility.
 */
public class TestPlayerComponent {
    
    public static void main(String[] args) {
        System.out.println("Testing Component-based Player Entity...");
        
        // Test 1: Player creation and initialization
        testPlayerCreation();
        
        // Test 2: Component integration
        testComponentIntegration();
        
        // Test 3: Input handling
        testInputHandling();
        
        // Test 4: Animation system
        testAnimationSystem();
        
        // Test 5: Legacy compatibility
        testLegacyCompatibility();
        
        System.out.println("All Player component tests completed!");
    }
    
    private static void testPlayerCreation() {
        System.out.println("\n=== Test 1: Player Creation ===");
        
        try {
            // Create mock dependencies
            GamePanel mockGamePanel = null; // We'll handle null checks in Player
            KeyHandler mockKeyHandler = new KeyHandler();
            
            // Create player
            Player player = new Player(mockGamePanel, mockKeyHandler);
            
            // Verify entity creation
            Entity entity = player.getEntity();
            assert entity != null : "Player entity should not be null";
            assert entity.isActive() : "Player entity should be active";
            
            // Verify components are attached
            assert entity.hasComponent(TransformComponent.class) : "Player should have TransformComponent";
            assert entity.hasComponent(RenderComponent.class) : "Player should have RenderComponent";
            assert entity.hasComponent(MovementComponent.class) : "Player should have MovementComponent";
            assert entity.hasComponent(CollisionComponent.class) : "Player should have CollisionComponent";
            assert entity.hasComponent(PlayerInputComponent.class) : "Player should have PlayerInputComponent";
            assert entity.hasComponent(AnimationComponent.class) : "Player should have AnimationComponent";
            
            System.out.println("✓ Player creation successful");
            System.out.println("✓ All required components attached");
            
        } catch (Exception e) {
            System.err.println("✗ Player creation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testComponentIntegration() {
        System.out.println("\n=== Test 2: Component Integration ===");
        
        try {
            KeyHandler keyHandler = new KeyHandler();
            Player player = new Player(null, keyHandler);
            
            // Test component access
            TransformComponent transform = player.getTransform();
            MovementComponent movement = player.getMovement();
            PlayerInputComponent input = player.getInput();
            AnimationComponent animation = player.getAnimation();
            
            assert transform != null : "Transform component should be accessible";
            assert movement != null : "Movement component should be accessible";
            assert input != null : "Input component should be accessible";
            assert animation != null : "Animation component should be accessible";
            
            // Test initial values
            assert transform.x == Config.TILE_SIZE * 23 : "Initial X position should be correct";
            assert transform.y == Config.TILE_SIZE * 21 : "Initial Y position should be correct";
            assert movement.maxSpeed == 5.0f : "Initial speed should be 5";
            assert input.getDirection() == Direction.DOWN : "Initial direction should be DOWN";
            
            System.out.println("✓ Component integration successful");
            System.out.println("✓ Initial values set correctly");
            
        } catch (Exception e) {
            System.err.println("✗ Component integration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testInputHandling() {
        System.out.println("\n=== Test 3: Input Handling ===");
        
        try {
            KeyHandler keyHandler = new KeyHandler();
            Player player = new Player(null, keyHandler);
            PlayerInputComponent input = player.getInput();
            
            // Test input processing
            input.processKeyPressed(input.getKeyBinding("move_up"));
            assert input.isUpPressed() : "Up key should be registered as pressed";
            assert input.getDirection() == Direction.UP : "Direction should change to UP";
            assert input.isMoving() : "Player should be moving";
            
            input.processKeyReleased(input.getKeyBinding("move_up"));
            assert !input.isUpPressed() : "Up key should be registered as released";
            assert !input.isMoving() : "Player should not be moving";
            
            // Test movement vector
            input.processKeyPressed(input.getKeyBinding("move_right"));
            input.processKeyPressed(input.getKeyBinding("move_down"));
            float[] moveVector = input.getMovementVector();
            
            // Should be normalized diagonal movement
            float expectedLength = (float) Math.sqrt(moveVector[0] * moveVector[0] + moveVector[1] * moveVector[1]);
            assert Math.abs(expectedLength - 1.0f) < 0.001f : "Movement vector should be normalized";
            
            System.out.println("✓ Input handling working correctly");
            System.out.println("✓ Movement vector normalization working");
            
        } catch (Exception e) {
            System.err.println("✗ Input handling failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testAnimationSystem() {
        System.out.println("\n=== Test 4: Animation System ===");
        
        try {
            KeyHandler keyHandler = new KeyHandler();
            Player player = new Player(null, keyHandler);
            AnimationComponent animation = player.getAnimation();
            
            // Test animation setup
            assert animation.getAnimation("walk") != null : "Walk animation should be loaded";
            assert animation.getAnimation("idle") != null : "Idle animation should be loaded";
            
            // Test animation playback
            animation.playAnimation("walk", Direction.UP);
            assert animation.isPlaying() : "Animation should be playing";
            assert animation.getCurrentAnimationName().equals("walk") : "Current animation should be 'walk'";
            assert animation.getCurrentDirection() == Direction.UP : "Animation direction should be UP";
            
            // Test animation update
            float deltaTime = 0.05f; // 50ms
            animation.update(deltaTime);
            // Animation should still be on first frame after 50ms (frame duration is 100ms)
            assert animation.getCurrentFrame() == 0 : "Should still be on first frame";
            
            animation.update(deltaTime); // Total 100ms
            // Now should advance to second frame
            assert animation.getCurrentFrame() == 1 : "Should advance to second frame";
            
            System.out.println("✓ Animation system working correctly");
            System.out.println("✓ Animation timing and frame advancement working");
            
        } catch (Exception e) {
            System.err.println("✗ Animation system failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testLegacyCompatibility() {
        System.out.println("\n=== Test 5: Legacy Compatibility ===");
        
        try {
            KeyHandler keyHandler = new KeyHandler();
            Player player = new Player(null, keyHandler);
            
            // Test legacy getters/setters
            int originalX = player.getWorldX();
            int originalY = player.getWorldY();
            
            player.setWorldX(100);
            player.setWorldY(200);
            
            assert player.getWorldX() == 100 : "Legacy setWorldX should work";
            assert player.getWorldY() == 200 : "Legacy setWorldY should work";
            assert player.getTransform().x == 100 : "Transform should be updated by legacy setter";
            assert player.getTransform().y == 200 : "Transform should be updated by legacy setter";
            
            // Test direction compatibility
            player.setDirection(Direction.LEFT);
            assert player.getDirection() == Direction.LEFT : "Legacy setDirection should work";
            assert player.getInput().getDirection() == Direction.LEFT : "Input component should be updated";
            
            // Test speed compatibility
            player.setSpeed(10);
            assert player.getSpeed() == 10 : "Legacy setSpeed should work";
            assert player.getMovement().maxSpeed == 10 : "Movement component should be updated";
            
            // Test solid area
            assert player.solidArea != null : "Solid area should exist for legacy compatibility";
            assert player.solidArea.width == 32 : "Solid area width should match original";
            assert player.solidArea.height == 32 : "Solid area height should match original";
            
            System.out.println("✓ Legacy compatibility maintained");
            System.out.println("✓ All legacy methods working correctly");
            
        } catch (Exception e) {
            System.err.println("✗ Legacy compatibility failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}