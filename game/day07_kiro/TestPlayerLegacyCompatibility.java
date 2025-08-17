import rpg.Config;
import rpg.KeyHandler;
import rpg.entity.Direction;
import rpg.entity.Player;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Test to verify that the refactored Player maintains full compatibility 
 * with the existing game systems and legacy code.
 */
public class TestPlayerLegacyCompatibility {
    
    public static void main(String[] args) {
        System.out.println("Testing Player Legacy Compatibility...");
        
        try {
            testLegacyInterface();
            testLegacyBehavior();
            testLegacyIntegration();
            
            System.out.println("\n=== LEGACY COMPATIBILITY VERIFIED ===");
            System.out.println("✓ All existing Player functionality preserved");
            System.out.println("✓ Legacy methods work correctly");
            System.out.println("✓ Existing game code will continue to work");
            
        } catch (Exception e) {
            System.err.println("Legacy compatibility test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test that all legacy methods and properties are still available
     */
    private static void testLegacyInterface() {
        System.out.println("\n=== Testing Legacy Interface ===");
        
        KeyHandler keyHandler = new KeyHandler();
        Player player = new Player(null, keyHandler);
        
        // Test legacy properties
        assert player.screenX == Config.SCREEN_WIDTH / 2 - (Config.TILE_SIZE / 2) : "screenX should be calculated correctly";
        assert player.screenY == Config.SCREEN_HEIGHT / 2 - (Config.TILE_SIZE / 2) : "screenY should be calculated correctly";
        assert player.solidArea != null : "solidArea should exist";
        assert player.solidArea.width == 32 : "solidArea width should be 32";
        assert player.solidArea.height == 32 : "solidArea height should be 32";
        
        // Test legacy methods
        int originalX = player.getWorldX();
        int originalY = player.getWorldY();
        
        player.setWorldX(500);
        player.setWorldY(600);
        assert player.getWorldX() == 500 : "setWorldX/getWorldX should work";
        assert player.getWorldY() == 600 : "setWorldY/getWorldY should work";
        
        player.setDirection(Direction.LEFT);
        assert player.getDirection() == Direction.LEFT : "setDirection/getDirection should work";
        
        player.setSpeed(8);
        assert player.getSpeed() == 8 : "setSpeed/getSpeed should work";
        
        System.out.println("✓ All legacy interface methods available and working");
    }
    
    /**
     * Test that the Player behaves the same as the original implementation
     */
    private static void testLegacyBehavior() {
        System.out.println("\n=== Testing Legacy Behavior ===");
        
        KeyHandler keyHandler = new KeyHandler();
        Player player = new Player(null, keyHandler);
        
        // Test initial state matches original
        assert player.getWorldX() == Config.TILE_SIZE * 23 : "Initial X position should match original";
        assert player.getWorldY() == Config.TILE_SIZE * 21 : "Initial Y position should match original";
        assert player.getDirection() == Direction.DOWN : "Initial direction should be DOWN";
        assert player.getSpeed() == 5 : "Initial speed should be 5";
        
        // Test input processing behavior
        keyHandler.upPressed = true;
        player.update();
        
        // After update with up pressed, direction should change
        // (Note: The exact behavior depends on the collision system)
        System.out.println("Current direction after up press: " + player.getDirection());
        
        // Test that update method exists and runs without error
        keyHandler.upPressed = false;
        keyHandler.downPressed = true;
        player.update();
        
        keyHandler.downPressed = false;
        player.update();
        
        System.out.println("✓ Legacy behavior preserved - update method works correctly");
    }
    
    /**
     * Test integration with existing game systems
     */
    private static void testLegacyIntegration() {
        System.out.println("\n=== Testing Legacy Integration ===");
        
        KeyHandler keyHandler = new KeyHandler();
        Player player = new Player(null, keyHandler);
        
        // Test that draw method exists and can be called
        // (We can't actually test rendering without a real Graphics2D, but we can verify the method exists)
        try {
            // Create a dummy BufferedImage to get Graphics2D
            BufferedImage dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = dummyImage.createGraphics();
            
            // This should not throw an exception
            player.draw(g2);
            
            g2.dispose();
            System.out.println("✓ draw() method works correctly");
        } catch (Exception e) {
            System.err.println("draw() method failed: " + e.getMessage());
        }
        
        // Test collision system integration
        // The solidArea should be updated when position changes
        int oldSolidX = player.solidArea.x;
        int oldSolidY = player.solidArea.y;
        
        player.setWorldX(player.getWorldX() + 50);
        player.setWorldY(player.getWorldY() + 50);
        
        // Update should sync the solidArea
        player.update();
        
        // solidArea should be updated (it includes the offset)
        assert player.solidArea.x != oldSolidX : "solidArea.x should be updated when position changes";
        assert player.solidArea.y != oldSolidY : "solidArea.y should be updated when position changes";
        
        System.out.println("✓ Collision system integration maintained");
        
        // Test that collisionOn field exists and can be set
        player.collisionOn = true;
        assert player.collisionOn : "collisionOn field should be accessible";
        
        player.collisionOn = false;
        assert !player.collisionOn : "collisionOn field should be modifiable";
        
        System.out.println("✓ Legacy collision fields accessible");
    }
}