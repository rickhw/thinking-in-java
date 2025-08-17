# Enhanced Tile and Map System

## Overview

The enhanced tile and map system provides a robust, extensible foundation for managing game maps and tiles. It replaces the simple array-based approach with a component-based architecture that supports multiple layers, tile properties, animations, and efficient rendering.

## Key Features

### 1. Enhanced TileSet System

- **Tile Properties**: Each tile can have custom properties including collision, type, and metadata
- **Animation Support**: Built-in support for animated tiles with configurable frame rates
- **Type Safety**: Strongly typed tile properties with validation
- **Extensible**: Easy to add new tile types and properties

```java
// Create a tileset with properties
TileSet tileSet = TileSet.createDefaultTileSet();

// Check tile properties
boolean isCollidable = tileSet.isCollidable(tileId);
TileType type = tileSet.getTileProperties(tileId).getType();
```

### 2. Multi-Layer Map System

- **Layer Support**: Maps can have multiple layers (background, foreground, collision, etc.)
- **Layer Properties**: Each layer has visibility, opacity, and custom properties
- **Efficient Storage**: Optimized tile data storage and access
- **Validation**: Comprehensive map validation with detailed error reporting

```java
// Load a map
GameMap map = GameMap.loadFromTextFile("/path/to/map.txt", tileSet);

// Access layers
MapLayer backgroundLayer = map.getLayer("background");
MapLayer collisionLayer = map.getLayer("collision");

// Check tile properties
boolean isCollidable = map.isCollidable(x, y);
int tileId = map.getTile("main", x, y);
```

### 3. Advanced Map Loading

- **Multiple Formats**: Support for text and CSV formats (extensible to JSON, TMX)
- **Error Handling**: Comprehensive error handling with detailed error messages
- **Validation**: Pre-loading validation to catch format errors early
- **Fallback Support**: Graceful handling of missing tiles and assets

```java
// Load with automatic format detection
GameMap map = GameMap.loadFromFile("/path/to/map.txt", tileSet);

// Validate before loading
List<String> errors = MapLoaderSimple.validateMapFile("/path/to/map.txt");
if (errors.isEmpty()) {
    GameMap map = GameMap.loadFromTextFile("/path/to/map.txt", tileSet);
}
```

### 4. Optimized Rendering

- **Viewport Culling**: Only renders visible tiles for better performance
- **Layer Rendering**: Proper layer ordering with opacity support
- **Animation Support**: Automatic animation frame selection based on time
- **Coordinate Conversion**: Efficient world-to-tile and tile-to-world conversion

```java
// Render with viewport culling
Rectangle viewBounds = new Rectangle(cameraX, cameraY, screenWidth, screenHeight);
map.render(graphics2D, viewBounds);

// Update animations
map.update(deltaTime);
```

## Architecture

### Class Hierarchy

```
TileSet
├── Tile (inner class)
│   ├── frames: BufferedImage[]
│   └── properties: TileProperties
└── TileProperties (inner class)
    ├── collidable: boolean
    ├── type: TileType
    ├── animated: boolean
    └── customProperties: Map<String, Object>

GameMap
├── layers: List<MapLayer>
├── tileSet: TileSet
└── MapLayer (inner class)
    ├── tileData: int[][]
    ├── visible: boolean
    └── opacity: float

MapLoaderSimple
├── loadTextMap()
├── validateMapFile()
└── extractMapName()
```

### Integration with Existing System

The enhanced system maintains backward compatibility through the updated `TileManager`:

```java
public class TileManager {
    // Legacy support
    public Tile[] tiles;
    public int mapTileNum[][];
    
    // Enhanced system
    private GameMap currentMap;
    private TileSet tileSet;
    private boolean useEnhancedSystem;
    
    // Unified interface
    public boolean isCollidable(int worldX, int worldY);
    public int getTileAt(int worldX, int worldY);
    public void draw(Graphics2D g2);
}
```

## Usage Examples

### Creating Custom Tile Properties

```java
// Create custom tile properties
Map<String, Object> customProps = new HashMap<>();
customProps.put("damage", 10);
customProps.put("sound", "water_splash");

TileProperties waterProps = new TileProperties(
    true,                    // collidable
    TileType.WATER,         // type
    customProps,            // custom properties
    true,                   // animated
    4,                      // animation frames
    2.0f                    // animation speed
);

// Add to tileset
tileSet.addTile(tileId, waterImage, waterProps);
```

### Working with Multiple Layers

```java
// Create a map with multiple layers
GameMap map = new GameMap("dungeon", 50, 50, 48, tileSet);

// Add background layer
MapLayer backgroundLayer = new MapLayer("background", 50, 50);
backgroundLayer.setTile(x, y, grassTileId);
map.addLayer(backgroundLayer);

// Add collision layer
MapLayer collisionLayer = new MapLayer("collision", 50, 50);
collisionLayer.setTile(x, y, wallTileId);
map.addLayer(collisionLayer);

// Check collision across all layers
boolean blocked = map.isCollidable(x, y);
```

### Custom Map Validation

```java
// Validate map integrity
List<String> errors = map.validate();
if (!errors.isEmpty()) {
    System.out.println("Map validation errors:");
    for (String error : errors) {
        System.out.println("  - " + error);
    }
}
```

## Performance Considerations

### Viewport Culling

The rendering system only processes tiles within the viewport:

```java
// Calculate visible tile range
int startX = Math.max(0, viewBounds.x / tileSize);
int endX = Math.min(width - 1, (viewBounds.x + viewBounds.width) / tileSize + 1);
int startY = Math.max(0, viewBounds.y / tileSize);
int endY = Math.min(height - 1, (viewBounds.y + viewBounds.height) / tileSize + 1);
```

### Memory Management

- Tiles are loaded once and cached in the TileSet
- Map data uses efficient int arrays for tile IDs
- Animation frames are pre-loaded and reused

### Coordinate Conversion

Efficient conversion between world and tile coordinates:

```java
public Point worldToTile(int worldX, int worldY) {
    return new Point(worldX / tileSize, worldY / tileSize);
}

public Point tileToWorld(int tileX, int tileY) {
    return new Point(tileX * tileSize, tileY * tileSize);
}
```

## Future Extensions

### Planned Features

1. **JSON Map Format**: Support for JSON-based map files with metadata
2. **TMX Support**: Integration with Tiled Map Editor format
3. **Tile Streaming**: Dynamic loading/unloading for large worlds
4. **Procedural Generation**: API for procedurally generated maps
5. **Tile Variants**: Support for tile variations and auto-tiling

### Extension Points

The system is designed for easy extension:

- Add new `TileType` enum values
- Implement new `MapLoader` formats
- Create custom `TileProperties` with additional metadata
- Add new layer types with specialized rendering

## Migration Guide

### From Legacy System

1. **Gradual Migration**: The system supports both legacy and enhanced modes
2. **Feature Flags**: Use `TileManager.isUsingEnhancedSystem()` to check mode
3. **API Compatibility**: Existing collision and rendering calls work unchanged
4. **Asset Compatibility**: Existing tile images and map files work without changes

### Best Practices

1. **Validate Early**: Always validate maps before loading in production
2. **Use Layers**: Separate different map elements into appropriate layers
3. **Cache TileSets**: Reuse TileSet instances across multiple maps
4. **Monitor Performance**: Use viewport culling for large maps
5. **Handle Errors**: Implement proper error handling for asset loading

## Testing

The system includes comprehensive tests:

- `TestTileSystem.java`: Core functionality tests
- `TestTileManagerIntegration.java`: Integration tests
- Map validation tests
- Performance benchmarks

Run tests with:
```bash
javac -cp . TestTileSystem.java
java -cp . TestTileSystem
```

## Conclusion

The enhanced tile and map system provides a solid foundation for 2D game development with:

- **Flexibility**: Support for complex map structures and tile properties
- **Performance**: Optimized rendering and memory usage
- **Extensibility**: Easy to add new features and formats
- **Reliability**: Comprehensive error handling and validation
- **Compatibility**: Seamless integration with existing code

This system addresses all the requirements for enhanced tile and map management while maintaining backward compatibility and providing a clear path for future enhancements.