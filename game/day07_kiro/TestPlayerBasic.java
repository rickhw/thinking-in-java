import rpg.components.*;
import rpg.engine.Entity;
import rpg.entity.Direction;

/**
 * Basic test for Player components without requiring full application context.
 */
public class TestPlayerBasic {
    
    public static void main(String[] args) {
        System.out.println("Testing Player Components...");
        
        try {
            // Test Entity creation
            Entity entity = new Entity();
            System.out.println("✓ Entity created successfully");
            
            // Test TransformComponent
            TransformComponent transform = new TransformComponent(100, 200);
            entity.addComponent(transform);
            assert entity.hasComponent(TransformComponent.class);
            assert entity.getComponent(TransformComponent.class).x == 100;
            System.out.println("✓ TransformComponent working");
            
            // Test MovementComponent
            MovementComponent movement = new MovementComponent(5.0f);
            entity.addComponent(movement);
            assert entity.hasComponent(MovementComponent.class);
            assert entity.getComponent(MovementComponent.class).maxSpeed == 5.0f;
            System.out.println("✓ MovementComponent working");
            
            // Test PlayerInputComponent
            PlayerInputComponent input = new PlayerInputComponent();
            entity.addComponent(input);
            assert entity.hasComponent(PlayerInputComponent.class);
            assert entity.getComponent(PlayerInputComponent.class).getDirection() == Direction.DOWN;
            System.out.println("✓ PlayerInputComponent working");
            
            // Test input processing
            input.processKeyPressed(input.getKeyBinding("move_up"));
            assert input.isUpPressed();
            assert input.getDirection() == Direction.UP;
            System.out.println("✓ Input processing working");
            
            // Test movement vector
            float[] moveVector = input.getMovementVector();
            assert moveVector[0] == 0 && moveVector[1] == -1;
            System.out.println("✓ Movement vector calculation working");
            
            // Test CollisionComponent
            CollisionComponent collision = new CollisionComponent(32, 32);
            entity.addComponent(collision);
            assert entity.hasComponent(CollisionComponent.class);
            assert entity.getComponent(CollisionComponent.class).getBounds().width == 32;
            System.out.println("✓ CollisionComponent working");
            
            // Test AnimationComponent
            AnimationComponent animation = new AnimationComponent();
            entity.addComponent(animation);
            assert entity.hasComponent(AnimationComponent.class);
            System.out.println("✓ AnimationComponent working");
            
            System.out.println("\n=== All basic component tests passed! ===");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}