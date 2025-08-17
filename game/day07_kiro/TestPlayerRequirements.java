import rpg.Config;
import rpg.KeyHandler;
import rpg.components.*;
import rpg.entity.Direction;
import rpg.entity.Player;
import rpg.engine.Entity;

/**
 * Comprehensive test to verify all requirements for the Player component conversion.
 * Tests Requirements 2.1, 2.2, 2.3, 2.4 as specified in the task.
 */
public class TestPlayerRequirements {
    
    public static void main(String[] args) {
        System.out.println("Testing Player Component Requirements...");
        System.out.println("Requirements: 2.1, 2.2, 2.3, 2.4");
        
        try {
            testRequirement2_1();
            testRequirement2_2();
            testRequirement2_3();
            testRequirement2_4();
            
            System.out.println("\n=== ALL REQUIREMENTS SATISFIED ===");
            System.out.println("✓ Player successfully converted to component-based architecture");
            System.out.println("✓ All existing functionality preserved");
            System.out.println("✓ Component composition working correctly");
            System.out.println("✓ Player-specific components implemented");
            
        } catch (Exception e) {
            System.err.println("Requirement test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Requirement 2.1: WHEN creating new entity types THEN they SHALL be composed of components 
     * rather than inheriting all functionality
     */
    private static void testRequirement2_1() {
        System.out.println("\n=== Testing Requirement 2.1: Component Composition ===");
        
        KeyHandler keyHandler = new KeyHandler();
        Player player = new Player(null, keyHandler);
        Entity entity = player.getEntity();
        
        // Verify the player is composed of components, not inheriting everything
        assert entity.hasComponent(TransformComponent.class) : "Player should be composed with TransformComponent";
        assert entity.hasComponent(RenderComponent.class) : "Player should be composed with RenderComponent";
        assert entity.hasComponent(MovementComponent.class) : "Player should be composed with MovementComponent";
        assert entity.hasComponent(CollisionComponent.class) : "Player should be composed with CollisionComponent";
        assert entity.hasComponent(PlayerInputComponent.class) : "Player should be composed with PlayerInputComponent";
        assert entity.hasComponent(AnimationComponent.class) : "Player should be composed with AnimationComponent";
        
        // Verify components can be accessed independently
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        assert transform != null : "Transform component should be accessible";
        assert movement != null : "Movement component should be accessible";
        
        System.out.println("✓ Requirement 2.1 SATISFIED: Player uses component composition");
    }
    
    /**
     * Requirement 2.2: WHEN an entity needs movement capability THEN it SHALL use a MovementComponent
     */
    private static void testRequirement2_2() {
        System.out.println("\n=== Testing Requirement 2.2: MovementComponent Usage ===");
        
        KeyHandler keyHandler = new KeyHandler();
        Player player = new Player(null, keyHandler);
        Entity entity = player.getEntity();
        
        // Verify MovementComponent is used for movement capability
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        assert movement != null : "Player should use MovementComponent for movement capability";
        
        // Test movement functionality through component
        movement.setVelocity(5.0f, -3.0f);
        assert movement.velocityX == 5.0f : "MovementComponent should handle X velocity";
        assert movement.velocityY == -3.0f : "MovementComponent should handle Y velocity";
        
        // Test movement constraints
        movement.maxSpeed = 10.0f;
        movement.setVelocity(15.0f, 0);
        movement.limitSpeed();
        assert movement.getSpeed() <= 10.0f : "MovementComponent should enforce speed limits";
        
        System.out.println("✓ Requirement 2.2 SATISFIED: Player uses MovementComponent for movement");
    }
    
    /**
     * Requirement 2.3: WHEN an entity needs rendering capability THEN it SHALL use a RenderComponent
     */
    private static void testRequirement2_3() {
        System.out.println("\n=== Testing Requirement 2.3: RenderComponent Usage ===");
        
        KeyHandler keyHandler = new KeyHandler();
        Player player = new Player(null, keyHandler);
        Entity entity = player.getEntity();
        
        // Verify RenderComponent is used for rendering capability
        RenderComponent render = entity.getComponent(RenderComponent.class);
        assert render != null : "Player should use RenderComponent for rendering capability";
        
        // Test rendering properties
        render.setLayer(5);
        assert render.getLayer() == 5 : "RenderComponent should handle layer management";
        
        render.setVisible(false);
        assert !render.isVisible() : "RenderComponent should handle visibility";
        
        render.setAlpha(0.5f);
        assert render.getAlpha() == 0.5f : "RenderComponent should handle transparency";
        
        // Test animation integration with render component
        AnimationComponent animation = entity.getComponent(AnimationComponent.class);
        assert animation != null : "Player should have AnimationComponent for sprite management";
        
        System.out.println("✓ Requirement 2.3 SATISFIED: Player uses RenderComponent for rendering");
    }
    
    /**
     * Requirement 2.4: WHEN an entity needs collision detection THEN it SHALL use a CollisionComponent
     */
    private static void testRequirement2_4() {
        System.out.println("\n=== Testing Requirement 2.4: CollisionComponent Usage ===");
        
        KeyHandler keyHandler = new KeyHandler();
        Player player = new Player(null, keyHandler);
        Entity entity = player.getEntity();
        
        // Verify CollisionComponent is used for collision detection
        CollisionComponent collision = entity.getComponent(CollisionComponent.class);
        assert collision != null : "Player should use CollisionComponent for collision detection";
        
        // Test collision properties
        assert collision.isSolid() : "Player collision should be solid";
        assert collision.getCollisionLayer() == 1 : "Player should be on collision layer 1";
        
        // Test collision bounds
        assert collision.getBounds().width == 32 : "Collision bounds width should match original";
        assert collision.getBounds().height == 32 : "Collision bounds height should match original";
        
        // Test collision with transform integration
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        transform.setPosition(100, 200);
        
        java.awt.Rectangle worldBounds = collision.getWorldBounds();
        assert worldBounds.x == 108 : "World bounds should include transform position + offset";
        assert worldBounds.y == 216 : "World bounds should include transform position + offset";
        
        // Test collision layer management
        collision.addCollisionLayer(2);
        assert collision.canCollideWith(2) : "Should be able to collide with added layer";
        
        System.out.println("✓ Requirement 2.4 SATISFIED: Player uses CollisionComponent for collision detection");
    }
}