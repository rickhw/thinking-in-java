import rpg.assets.GameMap;
import rpg.assets.MapLoaderSimple;
import rpg.assets.TileSet;
import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import java.util.List;

/**
 * Test class for the enhanced tile and map system.
 */
public class TestTileSystem {
    
    public static void main(String[] args) {
        System.out.println("Testing Enhanced Tile and Map System");
        System.out.println("====================================");
        
        try {
            // Test 1: Create default tileset
            System.out.println("\n1. Testing TileSet creation...");
            TileSet tileSet = TileSet.createDefaultTileSet();
            System.out.println("✓ Created default tileset with " + tileSet.getTileCount() + " tiles");
            
            // Test tile properties
            System.out.println("  - Grass tile (0) collidable: " + tileSet.isCollidable(0));
            System.out.println("  - Wall tile (1) collidable: " + tileSet.isCollidable(1));
            System.out.println("  - Water tile (2) collidable: " + tileSet.isCollidable(2));
            
            // Test 2: Validate map file
            System.out.println("\n2. Testing map file validation...");
            String mapPath = "/rpg/assets/maps/world01.txt";
            List<String> validationErrors = MapLoaderSimple.validateMapFile(mapPath);
            
            if (validationErrors.isEmpty()) {
                System.out.println("✓ Map file validation passed");
            } else {
                System.out.println("⚠ Map file validation issues:");
                for (String error : validationErrors) {
                    System.out.println("  - " + error);
                }
            }
            
            // Test 3: Load map
            System.out.println("\n3. Testing map loading...");
            
            // Test 4: Test map properties
            GameMap map = GameMap.loadFromTextFile(mapPath, tileSet);
            System.out.println("✓ Loaded map: " + map.getName());
            System.out.println("  - Dimensions: " + map.getWidth() + "x" + map.getHeight());
            System.out.println("  - World size: " + map.getWorldWidth() + "x" + map.getWorldHeight());
            System.out.println("  - Layers: " + map.getLayers().size());
            
            // Test 5: Test tile access
            System.out.println("\n5. Testing tile access...");
            int testX = 10, testY = 10;
            int tileId = map.getTile(testX, testY);
            boolean collidable = map.isCollidable(testX, testY);
            System.out.println("✓ Tile at (" + testX + "," + testY + "): ID=" + tileId + ", collidable=" + collidable);
            
            // Test 6: Test coordinate conversion
            System.out.println("\n6. Testing coordinate conversion...");
            java.awt.Point worldCoords = map.tileToWorld(testX, testY);
            java.awt.Point tileCoords = map.worldToTile(worldCoords.x, worldCoords.y);
            System.out.println("✓ Tile (" + testX + "," + testY + ") -> World (" + worldCoords.x + "," + worldCoords.y + ") -> Tile (" + tileCoords.x + "," + tileCoords.y + ")");
            
            // Test 7: Test map validation
            System.out.println("\n7. Testing map validation...");
            List<String> mapValidationErrors = map.validate();
            if (mapValidationErrors.isEmpty()) {
                System.out.println("✓ Map validation passed");
            } else {
                System.out.println("⚠ Map validation issues:");
                for (String error : mapValidationErrors) {
                    System.out.println("  - " + error);
                }
            }
            
            // Test 8: Test animation update
            System.out.println("\n8. Testing animation system...");
            float deltaTime = 0.016f; // ~60 FPS
            map.update(deltaTime);
            System.out.println("✓ Animation time updated to: " + map.getAnimationTime());
            
            System.out.println("\n✅ All tests completed successfully!");
            
        } catch (AssetLoadException e) {
            System.err.println("❌ Test failed with AssetLoadException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Test failed with unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}