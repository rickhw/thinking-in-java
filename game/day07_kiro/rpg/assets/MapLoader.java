package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MapLoader utility class that supports loading maps from multiple formats
 * with comprehensive error handling and validation.
 */
public class MapLoader {
    
    public enum MapFormat {
        TEXT,           // Simple space-separated text format
        CSV,            // Comma-separated values
        JSON,           // JSON format (future extension)
        TMX             // Tiled Map Editor format (future extension)
    }
    
    /**
     * Auto-detect map format from file extension.
     */
    public static MapFormat detectFormat(String mapPath) {
        String extension = mapPath.substring(mapPath.lastIndexOf('.') + 1).toLowerCase();
        
        switch (extension) {
            case "txt":
                return MapFormat.TEXT;
            case "csv":
                return MapFormat.CSV;
            case "json":
                return MapFormat.JSON;
            case "tmx":
                return MapFormat.TMX;
            default:
                return MapFormat.TEXT; // Default fallback
        }
    }
    
    /**
     * Load map from file with auto-format detection.
     */
    public static GameMap loadMap(String mapPath, TileSet tileSet) throws AssetLoadException {
        MapFormat format = detectFormat(mapPath);
        return loadMap(mapPath, tileSet, format);
    }
    
    /**
     * Load map from file with specified format.
     */
    public static GameMap loadMap(String mapPath, TileSet tileSet, MapFormat format) throws AssetLoadException {
        switch (format) {
            case TEXT:
                return loadTextMap(mapPath, tileSet);
            case CSV:
                return loadCSVMap(mapPath, tileSet);
            case JSON:
                throw new AssetLoadException(mapPath, "map", "JSON format not yet implemented");
            case TMX:
                throw new AssetLoadException(mapPath, "map", "TMX format not yet implemented");
            default:
                throw new AssetLoadException(mapPath, "map", "Unsupported map format: " + format);
        }
    }
    
    /**
     * Load map from simple text format (space-separated values).
     */
    private static GameMap loadTextMap(String mapPath, TileSet tileSet) throws AssetLoadException {
        try {
            InputStream is = MapLoader.class.getResourceAsStream(mapPath);
            if (is == null) {
                throw new AssetLoadException("Could not find map file: " + mapPath);
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
                throw new AssetLoadException("Map file is empty or contains no valid data: " + mapPath);
            }
            
            // Parse dimensions from first line
            String[] firstLineTokens = lines.get(0).split("\\s+");
            int width = firstLineTokens.length;
            int height = lines.size();
            
            // Validate dimensions
            if (width <= 0 || height <= 0) {
                throw new AssetLoadException("Invalid map dimensions: " + width + "x" + height);
            }
            
            // Create map
            String mapName = extractMapName(mapPath);
            GameMap map = new GameMap(mapName, width, height, tileSet.getTileSize(), tileSet);
            GameMap.MapLayer mainLayer = new GameMap.MapLayer("main", width, height);
            
            // Parse tile data with validation
            for (int y = 0; y < height; y++) {
                String[] tokens = lines.get(y).split("\\s+");
                if (tokens.length != width) {
                    throw new AssetLoadException(
                        String.format("Inconsistent row width at line %d: expected %d, got %d in map: %s", 
                                    y + 1, width, tokens.length, mapPath));
                }
                
                for (int x = 0; x < width; x++) {
                    try {
                        int tileId = Integer.parseInt(tokens[x]);
                        
                        // Validate tile ID
                        if (tileId < -1) {
                            throw new AssetLoadException(
                                String.format("Invalid tile ID %d at (%d,%d) in map: %s", 
                                            tileId, x, y, mapPath));
                        }
                        
                        // Warn about missing tiles (but don't fail)
                        if (tileId >= 0 && !tileSet.hasTile(tileId)) {
                            GameLogger.warn(
                                String.format("Tile ID %d at (%d,%d) not found in tileset for map: %s", 
                                            tileId, x, y, mapPath));
                        }
                        
                        mainLayer.setTile(x, y, tileId);
                        
                    } catch (NumberFormatException e) {
                        throw new AssetLoadException(
                            String.format("Invalid tile ID '%s' at (%d,%d) in map: %s", 
                                        tokens[x], x, y, mapPath), e);
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
            throw new AssetLoadException("Failed to read map file: " + mapPath, e);
        }
    }
    
    /**
     * Load map from CSV format (comma-separated values).
     */
    private static GameMap loadCSVMap(String mapPath, TileSet tileSet) throws AssetLoadException {
        try {
            InputStream is = MapLoader.class.getResourceAsStream(mapPath);
            if (is == null) {
                throw new AssetLoadException("Could not find map file: " + mapPath);
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
                throw new AssetLoadException("Map file is empty or contains no valid data: " + mapPath);
            }
            
            // Parse dimensions from first line
            String[] firstLineTokens = lines.get(0).split(",");
            int width = firstLineTokens.length;
            int height = lines.size();
            
            // Validate dimensions
            if (width <= 0 || height <= 0) {
                throw new AssetLoadException("Invalid map dimensions: " + width + "x" + height);
            }
            
            // Create map
            String mapName = extractMapName(mapPath);
            GameMap map = new GameMap(mapName, width, height, tileSet.getTileSize(), tileSet);
            GameMap.MapLayer mainLayer = new GameMap.MapLayer("main", width, height);
            
            // Parse tile data with validation
            for (int y = 0; y < height; y++) {
                String[] tokens = lines.get(y).split(",");
                if (tokens.length != width) {
                    throw new AssetLoadException(
                        String.format("Inconsistent row width at line %d: expected %d, got %d in map: %s", 
                                    y + 1, width, tokens.length, mapPath));
                }
                
                for (int x = 0; x < width; x++) {
                    try {
                        String token = tokens[x].trim();
                        int tileId = Integer.parseInt(token);
                        
                        // Validate tile ID
                        if (tileId < -1) {
                            throw new AssetLoadException(
                                String.format("Invalid tile ID %d at (%d,%d) in map: %s", 
                                            tileId, x, y, mapPath));
                        }
                        
                        // Warn about missing tiles (but don't fail)
                        if (tileId >= 0 && !tileSet.hasTile(tileId)) {
                            GameLogger.warn(
                                String.format("Tile ID %d at (%d,%d) not found in tileset for map: %s", 
                                            tileId, x, y, mapPath));
                        }
                        
                        mainLayer.setTile(x, y, tileId);
                        
                    } catch (NumberFormatException e) {
                        throw new AssetLoadException(
                            String.format("Invalid tile ID '%s' at (%d,%d) in map: %s", 
                                        tokens[x].trim(), x, y, mapPath), e);
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
            
            GameLogger.info(String.format("Loaded CSV map '%s' (%dx%d)", mapPath, width, height));
            return map;
            
        } catch (IOException e) {
            throw new AssetLoadException("Failed to read map file: " + mapPath, e);
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
            InputStream is = MapLoader.class.getResourceAsStream(mapPath);
            if (is == null) {
                errors.add("Map file not found: " + mapPath);
                return errors;
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            List<String> lines = new ArrayList<>();
            String line;
            int lineNumber = 0;
            
            // Read and validate each line
            while ((line = br.readLine()) != null) {
                lineNumber++;
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
            MapFormat format = detectFormat(mapPath);
            String delimiter = (format == MapFormat.CSV) ? "," : "\\s+";
            
            String[] firstLineTokens = lines.get(0).split(delimiter);
            int expectedWidth = firstLineTokens.length;
            
            if (expectedWidth <= 0) {
                errors.add("Invalid first line in map file");
                return errors;
            }
            
            // Check all lines have consistent width
            for (int i = 0; i < lines.size(); i++) {
                String[] tokens = lines.get(i).split(delimiter);
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
    
    /**
     * Get map information without fully loading it.
     */
    public static MapInfo getMapInfo(String mapPath) throws AssetLoadException {
        try {
            InputStream is = MapLoader.class.getResourceAsStream(mapPath);
            if (is == null) {
                throw new AssetLoadException("Could not find map file: " + mapPath);
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String firstLine = null;
            int lineCount = 0;
            
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    if (firstLine == null) {
                        firstLine = line;
                    }
                    lineCount++;
                }
            }
            br.close();
            
            if (firstLine == null) {
                throw new AssetLoadException("Map file contains no valid data: " + mapPath);
            }
            
            MapFormat format = detectFormat(mapPath);
            String delimiter = (format == MapFormat.CSV) ? "," : "\\s+";
            String[] tokens = firstLine.split(delimiter);
            
            return new MapInfo(extractMapName(mapPath), tokens.length, lineCount, format);
            
        } catch (IOException e) {
            throw new AssetLoadException("Failed to read map file: " + mapPath, e);
        }
    }
    
    /**
     * Map information structure.
     */
    public static class MapInfo {
        public final String name;
        public final int width;
        public final int height;
        public final MapFormat format;
        
        public MapInfo(String name, int width, int height, MapFormat format) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.format = format;
        }
        
        @Override
        public String toString() {
            return String.format("MapInfo{name='%s', size=%dx%d, format=%s}", 
                               name, width, height, format);
        }
    }
}