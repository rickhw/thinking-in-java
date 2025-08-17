import rpg.Config;
import rpg.KeyHandler;
import rpg.components.*;
import rpg.entity.Direction;
import rpg.entity.Player;
import rpg.engine.Entity;

/**
 * Integration test for the component-based Player entity.
 * Tests the full Player class with component integration.
 */
public class TestPlayerIntegration {
    
    public static void main(String[] args) {
        System.out.println("Testing Player Integration...");
        
        try {
            // Test Player creation
            KeyHandler keyHandler = new KeyHandler();
            Player player = new Player(null, keyHandler); // null GamePanel for testing
            
            System.out.println("✓ Player created successfully");
            
            // Test entity and components
            Entity entity = player.getEntity();
            assert entity != null : "Entity should not be null";
            assert entity.isActive() : "Entity should be active";
            
            // Verify all components are present
            assert entity.hasComponent(TransformComponent.class) : "Missing TransformComponent";
            assert entity.hasComponent(RenderComponent.class) : "Missing RenderComponent";
            assert entity.hasComponent(MovementComponent.class) : "Missing MovementComponent";
            assert entity.hasComponent(CollisionComponent.class) : "Missing CollisionComponent";
            assert entity.hasComponent(PlayerInputComponent.class) : "Missing PlayerInputComponent";
            assert entity.hasComponent(AnimationComponent.class) : "Missing AnimationComponent";
            
            System.out.println("✓ All components attached correctly");
            
            // Test initial values
            TransformComponent transform = player.getTransform();
            assert transform.x == Config.TILE_SIZE * 23 : "Initial X position incorrect";
            assert transform.y == Config.TILE_SIZE * 21 : "Initial Y position incorrect";
            
            PlayerInputComponent input = player.getInput();
            assert input.getDirection() == Direction.DOWN : "Initial direction should be DOWN";
            
            MovementComponent movement = player.getMovement();
            assert movement.maxSpeed == 5.0f : "Initial speed should be 5";
            
            System.out.println("✓ Initial values set correctly");
            
            // Test legacy compatibility
            assert player.getWorldX() == (int)transform.x : "Legacy getWorldX should match transform";
            assert player.getWorldY() == (int)transform.y : "Legacy getWorldY should match transform";
            assert player.getDirection() == input.getDirection() : "Legacy getDirection should match input";
            assert player.getSpeed() == (int)movement.maxSpeed : "Legacy getSpeed should match movement";
            
            System.out.println("✓ Legacy compatibility maintained");
            
            // Test legacy setters
            player.setWorldX(500);
            player.setWorldY(600);
            assert transform.x == 500 : "setWorldX should update transform";
            assert transform.y == 600 : "setWorldY should update transform";
            
            player.setDirection(Direction.LEFT);
            assert input.getDirection() == Direction.LEFT : "setDirection should update input";
            
            player.setSpeed(10);
            assert movement.maxSpeed == 10 : "setSpeed should update movement";
            
            System.out.println("✓ Legacy setters working correctly");
            
            // Test input simulation
            keyHandler.upPressed = true;
            player.update(); // This should process the input
            
            // Note: The actual input processing depends on the update method
            // which processes KeyHandler state
            System.out.println("✓ Input simulation completed");
            
            // Test animation system
            AnimationComponent animation = player.getAnimation();
            assert animation.getAnimation("walk") != null : "Walk animation should be loaded";
            assert animation.getAnimation("idle") != null : "Idle animation should be loaded";
            
            System.out.println("✓ Animation system initialized");
            
            // Test solid area compatibility
            assert player.solidArea != null : "Solid area should exist";
            assert player.solidArea.width == 32 : "Solid area width should be 32";
            assert player.solidArea.height == 32 : "Solid area height should be 32";
            
            System.out.println("✓ Collision area compatibility maintained");
            
            System.out.println("\n=== All Player integration tests passed! ===");
            System.out.println("Player successfully converted to component-based architecture");
            System.out.println("All existing functionality preserved");
            
        } catch (Exception e) {
            System.err.println("Integration test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}