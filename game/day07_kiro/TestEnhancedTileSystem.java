import rpg.tile.TileManager;
import rpg.GamePanel;
import rpg.assets.GameMap;
import rpg.assets.TileSet;
import rpg.utils.GameLogger;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Test class for the enhanced tile system integration with TileManager.
 */
public class TestEnhancedTileSystem {
    
    public static void main(String[] args) {
        System.out.println("Testing Enhanced Tile System Integration");
        System.out.println("=======================================");
        
        try {
            // Create a mock GamePanel for testing
            MockGamePanel mockGamePanel = new MockGamePanel();
            
            // Test 1: Initialize TileManager with enhanced system
            System.out.println("\n1. Testing TileManager initialization...");
            TileManager tileManager = new TileManager(mockGamePanel);
            System.out.println("✓ TileManager initialized");
            System.out.println("  - Using enhanced system: " + tileManager.isUsingEnhancedSystem());
            
            if (tileManager.isUsingEnhancedSystem()) {
                GameMap currentMap = tileManager.getCurrentMap();
                TileSet tileSet = tileManager.getTileSet();
                
                System.out.println("  - Current map: " + currentMap.getName());
                System.out.println("  - Map dimensions: " + currentMap.getWidth() + "x" + currentMap.getHeight());
                System.out.println("  - TileSet tiles: " + tileSet.getTileCount());
            }
            
            // Test 2: Test collision detection
            System.out.println("\n2. Testing collision detection...");
            int testWorldX = 480; // 10 tiles * 48 pixels
            int testWorldY = 480;
            boolean isCollidable = tileManager.isCollidable(testWorldX, testWorldY);
            int tileId = tileManager.getTileAt(testWorldX, testWorldY);
            System.out.println("✓ Collision test at world (" + testWorldX + "," + testWorldY + ")");
            System.out.println("  - Tile ID: " + tileId);
            System.out.println("  - Collidable: " + isCollidable);
            
            // Test 3: Test animation update
            System.out.println("\n3. Testing animation update...");
            float deltaTime = 0.016f; // ~60 FPS
            tileManager.update(deltaTime);
            System.out.println("✓ Animation update completed");
            
            // Test 4: Test rendering (mock)
            System.out.println("\n4. Testing rendering...");
            BufferedImage testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = testImage.createGraphics();
            
            // Set up mock player position for rendering
            mockGamePanel.player.worldX = 1200; // Center of map
            mockGamePanel.player.worldY = 1200;
            mockGamePanel.player.screenX = 400;
            mockGamePanel.player.screenY = 300;
            
            tileManager.draw(g2);
            g2.dispose();
            System.out.println("✓ Rendering test completed");
            
            // Test 5: Test map loading
            System.out.println("\n5. Testing map loading...");
            tileManager.loadMap("/rpg/assets/maps/world01.txt", true);
            System.out.println("✓ Map loading test completed");
            
            System.out.println("\n✅ All integration tests completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Mock GamePanel for testing purposes.
     */
    static class MockGamePanel extends GamePanel {
        public MockGamePanel() {
            super();
            // Initialize mock player
            this.player = new MockPlayer();
        }
    }
    
    /**
     * Mock Player for testing purposes.
     */
    static class MockPlayer {
        public int worldX = 1200;
        public int worldY = 1200;
        public int screenX = 400;
        public int screenY = 300;
    }
}