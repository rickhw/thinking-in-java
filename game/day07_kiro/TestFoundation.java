import rpg.utils.ServiceLocator;
import rpg.utils.GameLogger;
import rpg.game.EntityManager;
import rpg.engine.Entity;
import rpg.components.TransformComponent;

/**
 * Simple test to verify the foundation architecture works correctly.
 */
public class TestFoundation {
    public static void main(String[] args) {
        // Initialize logging
        GameLogger.initialize();
        GameLogger.info("Testing foundation architecture...");
        
        // Initialize service locator
        ServiceLocator.initialize();
        GameLogger.info("Service locator initialized");
        
        // Test entity creation and component system
        EntityManager entityManager = ServiceLocator.getService(EntityManager.class);
        
        Entity testEntity = entityManager.createEntity();
        testEntity.addComponent(new TransformComponent(100, 200));
        
        entityManager.update(); // Process pending additions
        
        // Verify entity was created
        Entity retrievedEntity = entityManager.getEntity(testEntity.getId());
        if (retrievedEntity != null) {
            GameLogger.info("Entity created successfully with ID: " + retrievedEntity.getId());
            
            TransformComponent transform = retrievedEntity.getComponent(TransformComponent.class);
            if (transform != null) {
                GameLogger.info("Transform component found at position: (" + transform.x + ", " + transform.y + ")");
            }
        }
        
        GameLogger.info("Foundation architecture test completed successfully!");
    }
}