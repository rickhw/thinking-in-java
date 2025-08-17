package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified MapLoader that focuses on text format loading with proper error handling.
 */
public class MapLoaderSimple {
    
    /**
     * Load map from simple text format (space-separated values).
     */
    public static GameMap loadTextMap(String mapPath, TileSet tileSet) throws AssetLoadException {
        try {
            InputStream is = MapLoaderSimple.class.getResourceAsStream(mapPath);
            if (is == null) {
                throw new AssetLoadException(mapPath, "map", "Could not find map file");
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            List<String> lines = new ArrayList<>();
            String line;
            
            // Read all non-empty lines
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) { // Skip comments
                    lines.add(line);
                }
            }
            br.close();
            
            if (lines.isEmpty()) {
                throw new AssetLoadException(mapPath, "map", "Map file is empty or contains no valid data");
            }
            
            // Parse dimensions from first line
            String[] firstLineTokens = lines.get(0).split("\\s+");
            int width = firstLineTokens.length;
            int height = lines.size();
            
            // Validate dimensions
            if (width <= 0 || height <= 0) {
                throw new AssetLoadException(mapPath, "map", "Invalid map dimensions: " + width + "x" + height);
            }
            
            // Create map
            String mapName = extractMapName(mapPath);
            GameMap map = new GameMap(mapName, width, height, tileSet.getTileSize(), tileSet);
            GameMap.MapLayer mainLayer = new GameMap.MapLayer("main", width, height);
            
            // Parse tile data with validation
            for (int y = 0; y < height; y++) {
                String[] tokens = lines.get(y).split("\\s+");
                if (tokens.length != width) {
                    throw new AssetLoadException(mapPath, "map", 
                        String.format("Inconsistent row width at line %d: expected %d, got %d", 
                                    y + 1, width, tokens.length));
                }
                
                for (int x = 0; x < width; x++) {
                    try {
                        int tileId = Integer.parseInt(tokens[x]);
                        
                        // Validate tile ID
                        if (tileId < -1) {
                            throw new AssetLoadException(mapPath, "map",
                                String.format("Invalid tile ID %d at (%d,%d)", tileId, x, y));
                        }
                        
                        // Warn about missing tiles (but don't fail)
                        if (tileId >= 0 && !tileSet.hasTile(tileId)) {
                            GameLogger.warn(
                                String.format("Tile ID %d at (%d,%d) not found in tileset for map: %s", 
                                            tileId, x, y, mapPath));
                        }
                        
                        mainLayer.setTile(x, y, tileId);
                        
                    } catch (NumberFormatException e) {
                        throw new AssetLoadException(mapPath, "map",
                            String.format("Invalid tile ID '%s' at (%d,%d)", tokens[x], x, y));
                    }
                }
            }
            
            map.addLayer(mainLayer);
            
            // Validate the loaded map
            List<String> validationErrors = map.validate();
            if (!validationErrors.isEmpty()) {
                GameLogger.warn("Map validation issues for " + mapPath + ":");
                for (String error : validationErrors) {
                    GameLogger.warn("  " + error);
                }
            }
            
            GameLogger.info(String.format("Loaded text map '%s' (%dx%d)", mapPath, width, height));
            return map;
            
        } catch (IOException e) {
            throw new AssetLoadException(mapPath, "map", e);
        }
    }
    
    /**
     * Extract map name from file path.
     */
    private static String extractMapName(String mapPath) {
        String fileName = mapPath.substring(mapPath.lastIndexOf('/') + 1);
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
    
    /**
     * Validate map file before loading.
     */
    public static List<String> validateMapFile(String mapPath) {
        List<String> errors = new ArrayList<>();
        
        try {
            InputStream is = MapLoaderSimple.class.getResourceAsStream(mapPath);
            if (is == null) {
                errors.add("Map file not found: " + mapPath);
                return errors;
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            List<String> lines = new ArrayList<>();
            String line;
            
            // Read and validate each line
            while ((line = br.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }
                
                lines.add(line);
            }
            br.close();
            
            if (lines.isEmpty()) {
                errors.add("Map file contains no valid data");
                return errors;
            }
            
            // Validate format consistency
            String[] firstLineTokens = lines.get(0).split("\\s+");
            int expectedWidth = firstLineTokens.length;
            
            if (expectedWidth <= 0) {
                errors.add("Invalid first line in map file");
                return errors;
            }
            
            // Check all lines have consistent width
            for (int i = 0; i < lines.size(); i++) {
                String[] tokens = lines.get(i).split("\\s+");
                if (tokens.length != expectedWidth) {
                    errors.add(String.format("Line %d has %d tokens, expected %d", 
                                           i + 1, tokens.length, expectedWidth));
                }
                
                // Validate each token is a valid integer
                for (int j = 0; j < tokens.length; j++) {
                    try {
                        Integer.parseInt(tokens[j].trim());
                    } catch (NumberFormatException e) {
                        errors.add(String.format("Invalid tile ID '%s' at line %d, column %d", 
                                                tokens[j].trim(), i + 1, j + 1));
                    }
                }
            }
            
        } catch (IOException e) {
            errors.add("Failed to read map file: " + e.getMessage());
        }
        
        return errors;
    }
}