import rpg.components.*;
import rpg.engine.Entity;
import rpg.entity.Direction;
import rpg.systems.PlayerController;

/**
 * Test for the PlayerController system.
 */
public class TestPlayerController {
    
    public static void main(String[] args) {
        System.out.println("Testing PlayerController System...");
        
        try {
            // Create a PlayerController
            PlayerController controller = new PlayerController();
            controller.initialize();
            
            System.out.println("✓ PlayerController created and initialized");
            
            // Create a test player entity
            Entity playerEntity = new Entity();
            
            // Add required components
            TransformComponent transform = new TransformComponent(100, 100);
            playerEntity.addComponent(transform);
            
            MovementComponent movement = new MovementComponent(5.0f);
            playerEntity.addComponent(movement);
            
            PlayerInputComponent input = new PlayerInputComponent();
            playerEntity.addComponent(input);
            
            AnimationComponent animation = new AnimationComponent();
            playerEntity.addComponent(animation);
            
            CollisionComponent collision = new CollisionComponent(32, 32);
            playerEntity.addComponent(collision);
            
            System.out.println("✓ Test player entity created with components");
            
            // Set the player entity in the controller
            controller.setPlayerEntity(playerEntity);
            assert controller.getPlayerEntity() == playerEntity : "Player entity should be set correctly";
            
            System.out.println("✓ Player entity set in controller");
            
            // Test controller utility methods
            controller.teleportPlayer(200, 300);
            assert transform.x == 200 : "Teleport should update X position";
            assert transform.y == 300 : "Teleport should update Y position";
            
            System.out.println("✓ Teleport functionality working");
            
            // Test direction setting
            controller.setPlayerDirection(Direction.RIGHT);
            assert controller.getPlayerDirection() == Direction.RIGHT : "Direction should be set correctly";
            
            System.out.println("✓ Direction setting working");
            
            // Test position getting
            float[] position = controller.getPlayerPosition();
            assert position[0] == 200 : "Position X should be correct";
            assert position[1] == 300 : "Position Y should be correct";
            
            System.out.println("✓ Position getting working");
            
            // Test update with no input
            controller.update(1.0f / 60.0f); // 60 FPS delta time
            
            System.out.println("✓ Update with no input completed");
            
            // Test update with input
            input.processKeyPressed(input.getKeyBinding("move_up"));
            controller.update(1.0f / 60.0f);
            
            System.out.println("✓ Update with input completed");
            
            // Test cleanup
            controller.cleanup();
            assert controller.getPlayerEntity() == null : "Player entity should be null after cleanup";
            
            System.out.println("✓ Cleanup working correctly");
            
            System.out.println("\n=== All PlayerController tests passed! ===");
            
        } catch (Exception e) {
            System.err.println("PlayerController test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}