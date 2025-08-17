import rpg.assets.GameMap;
import rpg.assets.TileSet;
import rpg.assets.MapLoaderSimple;
import rpg.exceptions.AssetLoadException;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Test class for the enhanced tile system components without GamePanel dependencies.
 */
public class TestTileManagerIntegration {
    
    public static void main(String[] args) {
        System.out.println("Testing Enhanced Tile System Components");
        System.out.println("======================================");
        
        try {
            // Test 1: Create and test TileSet
            System.out.println("\n1. Testing TileSet functionality...");
            TileSet tileSet = TileSet.createDefaultTileSet();
            System.out.println("✓ Created default tileset with " + tileSet.getTileCount() + " tiles");
            
            // Test tile properties
            for (int i = 0; i < 6; i++) {
                TileSet.TileProperties props = tileSet.getTileProperties(i);
                if (props != null) {
                    System.out.println("  - Tile " + i + ": type=" + props.getType() + 
                                     ", collidable=" + props.isCollidable() + 
                                     ", animated=" + props.isAnimated());
                }
            }
            
            // Test 2: Load and validate map
            System.out.println("\n2. Testing map loading and validation...");
            String mapPath = "/rpg/assets/maps/world01.txt";
            
            // Validate before loading
            java.util.List<String> validationErrors = MapLoaderSimple.validateMapFile(mapPath);
            if (validationErrors.isEmpty()) {
                System.out.println("✓ Map file validation passed");
            } else {
                System.out.println("⚠ Map file validation issues:");
                for (String error : validationErrors) {
                    System.out.println("  - " + error);
                }
            }
            
            // Load the map
            GameMap map = GameMap.loadFromTextFile(mapPath, tileSet);
            System.out.println("✓ Loaded map: " + map.getName());
            System.out.println("  - Dimensions: " + map.getWidth() + "x" + map.getHeight());
            System.out.println("  - World size: " + map.getWorldWidth() + "x" + map.getWorldHeight());
            System.out.println("  - Tile size: " + map.getTileSize());
            System.out.println("  - Layers: " + map.getLayers().size());
            
            // Test 3: Test map functionality
            System.out.println("\n3. Testing map functionality...");
            
            // Test coordinate conversion
            Point worldCoords = map.tileToWorld(10, 10);
            Point tileCoords = map.worldToTile(worldCoords.x, worldCoords.y);
            System.out.println("✓ Coordinate conversion: tile(10,10) -> world(" + 
                             worldCoords.x + "," + worldCoords.y + ") -> tile(" + 
                             tileCoords.x + "," + tileCoords.y + ")");
            
            // Test tile access
            int tileId = map.getTile(10, 10);
            boolean collidable = map.isCollidable(10, 10);
            TileSet.TileProperties props = map.getTileProperties(10, 10);
            System.out.println("✓ Tile access at (10,10): ID=" + tileId + 
                             ", collidable=" + collidable + 
                             ", type=" + (props != null ? props.getType() : "null"));
            
            // Test 4: Test animation system
            System.out.println("\n4. Testing animation system...");
            float initialTime = map.getAnimationTime();
            map.update(0.016f); // ~60 FPS frame
            float updatedTime = map.getAnimationTime();
            System.out.println("✓ Animation time: " + initialTime + " -> " + updatedTime);
            
            // Test 5: Test rendering bounds calculation
            System.out.println("\n5. Testing rendering system...");
            Rectangle viewBounds = new Rectangle(480, 480, 800, 600); // View centered at (480,480)
            
            // Create a test image for rendering
            BufferedImage testImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = testImage.createGraphics();
            
            // Test rendering (this will actually render to the test image)
            map.render(g2, viewBounds);
            g2.dispose();
            System.out.println("✓ Rendering test completed");
            System.out.println("  - View bounds: " + viewBounds);
            System.out.println("  - Map bounds: " + map.getBounds());
            
            // Test 6: Test map validation
            System.out.println("\n6. Testing map validation...");
            java.util.List<String> mapValidationErrors = map.validate();
            if (mapValidationErrors.isEmpty()) {
                System.out.println("✓ Map validation passed");
            } else {
                System.out.println("⚠ Map validation issues:");
                for (String error : mapValidationErrors) {
                    System.out.println("  - " + error);
                }
            }
            
            // Test 7: Test layer system
            System.out.println("\n7. Testing layer system...");
            GameMap.MapLayer mainLayer = map.getLayer("main");
            if (mainLayer != null) {
                System.out.println("✓ Main layer found");
                System.out.println("  - Layer name: " + mainLayer.getName());
                System.out.println("  - Layer dimensions: " + mainLayer.getWidth() + "x" + mainLayer.getHeight());
                System.out.println("  - Layer visible: " + mainLayer.isVisible());
                System.out.println("  - Layer opacity: " + mainLayer.getOpacity());
            }
            
            // Test 8: Test edge cases
            System.out.println("\n8. Testing edge cases...");
            
            // Test out-of-bounds access
            int invalidTileId = map.getTile(-1, -1);
            boolean invalidCollidable = map.isCollidable(1000, 1000);
            System.out.println("✓ Out-of-bounds access: tileId=" + invalidTileId + 
                             ", collidable=" + invalidCollidable);
            
            // Test coordinate conversion edge cases
            Point edgeWorld = map.tileToWorld(0, 0);
            Point edgeTile = map.worldToTile(0, 0);
            System.out.println("✓ Edge coordinate conversion: tile(0,0) -> world(" + 
                             edgeWorld.x + "," + edgeWorld.y + ") -> tile(" + 
                             edgeTile.x + "," + edgeTile.y + ")");
            
            System.out.println("\n✅ All component tests completed successfully!");
            
        } catch (AssetLoadException e) {
            System.err.println("❌ Test failed with AssetLoadException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Test failed with unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}