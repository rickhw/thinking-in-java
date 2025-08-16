package rpg.assets;

import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;
import rpg.utils.ServiceLocator;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all game assets with type-safe access and lifecycle management.
 * Integrates with AssetManager and provides convenient access methods.
 */
public class AssetRegistry {
    private static final GameLogger logger = GameLogger.getInstance();
    
    // Asset registries by type
    private final Map<String, BufferedImage> images = new ConcurrentHashMap<>();
    private final Map<String, TextureAtlas> atlases = new ConcurrentHashMap<>();
    private final Map<String, SpriteSheet> spriteSheets = new ConcurrentHashMap<>();
    private final Map<String, TileSet> tileSets = new ConcurrentHashMap<>();
    private final Map<String, GameMap> maps = new ConcurrentHashMap<>();
    private final Map<String, SpriteAnimationData> animations = new ConcurrentHashMap<>();
    
    // Asset metadata
    private final Map<String, AssetMetadata> metadata = new ConcurrentHashMap<>();
    
    // Asset manager reference
    private final AssetManager assetManager;
    
    // Singleton instance
    private static AssetRegistry instance;
    
    private AssetRegistry() {
        this.assetManager = AssetManager.getInstance();
    }
    
    /**
     * Get the singleton instance.
     */
    public static synchronized AssetRegistry getInstance() {
        if (instance == null) {
            instance = new AssetRegistry();
            // Register with service locator
            ServiceLocator.registerService(AssetRegistry.class, instance);
        }
        return instance;
    }
    
    /**
     * Register an image asset.
     */
    public void registerImage(String name, String path) throws AssetLoadException {
        BufferedImage image = assetManager.loadImage(path);
        images.put(name, image);
        metadata.put(name, new AssetMetadata(name, path, AssetType.IMAGE));
        
        logger.debug("Registered image: " + name + " from " + path);
    }
    
    /**
     * Register a texture atlas.
     */
    public void registerAtlas(String name, String path) throws AssetLoadException {
        TextureAtlas atlas = assetManager.loadAsset(path, TextureAtlas.class);
        atlases.put(name, atlas);
        metadata.put(name, new AssetMetadata(name, path, AssetType.ATLAS));
        
        logger.debug("Registered atlas: " + name + " from " + path);
    }
    
    /**
     * Register a sprite sheet.
     */
    public void registerSpriteSheet(String name, String path, int spriteWidth, int spriteHeight) throws AssetLoadException {
        SpriteSheet spriteSheet = AssetLoader.loadSpriteSheet(path, spriteWidth, spriteHeight);
        spriteSheets.put(name, spriteSheet);
        metadata.put(name, new AssetMetadata(name, path, AssetType.SPRITE_SHEET));
        
        logger.debug("Registered sprite sheet: " + name + " from " + path);
    }
    
    /**
     * Register a tileset.
     */
    public void registerTileSet(String name, String path) throws AssetLoadException {
        TileSet tileSet = assetManager.loadTileSet(path);
        tileSets.put(name, tileSet);
        metadata.put(name, new AssetMetadata(name, path, AssetType.TILESET));
        
        logger.debug("Registered tileset: " + name + " from " + path);
    }
    
    /**
     * Register a game map.
     */
    public void registerMap(String name, String path) throws AssetLoadException {
        GameMap map = assetManager.loadMap(path);
        maps.put(name, map);
        metadata.put(name, new AssetMetadata(name, path, AssetType.MAP));
        
        logger.debug("Registered map: " + name + " from " + path);
    }
    
    /**
     * Register sprite animation data.
     */
    public void registerAnimation(String name, SpriteAnimationData animationData) {
        animations.put(name, animationData);
        metadata.put(name, new AssetMetadata(name, null, AssetType.ANIMATION));
        
        logger.debug("Registered animation: " + name);
    }
    
    /**
     * Get an image by name.
     */
    public BufferedImage getImage(String name) {
        BufferedImage image = images.get(name);
        if (image == null) {
            logger.warn("Image not found in registry: " + name);
        }
        return image;
    }
    
    /**
     * Get a texture atlas by name.
     */
    public TextureAtlas getAtlas(String name) {
        TextureAtlas atlas = atlases.get(name);
        if (atlas == null) {
            logger.warn("Atlas not found in registry: " + name);
        }
        return atlas;
    }
    
    /**
     * Get a sprite sheet by name.
     */
    public SpriteSheet getSpriteSheet(String name) {
        SpriteSheet spriteSheet = spriteSheets.get(name);
        if (spriteSheet == null) {
            logger.warn("Sprite sheet not found in registry: " + name);
        }
        return spriteSheet;
    }
    
    /**
     * Get a tileset by name.
     */
    public TileSet getTileSet(String name) {
        TileSet tileSet = tileSets.get(name);
        if (tileSet == null) {
            logger.warn("Tileset not found in registry: " + name);
        }
        return tileSet;
    }
    
    /**
     * Get a game map by name.
     */
    public GameMap getMap(String name) {
        GameMap map = maps.get(name);
        if (map == null) {
            logger.warn("Map not found in registry: " + name);
        }
        return map;
    }
    
    /**
     * Get sprite animation data by name.
     */
    public SpriteAnimationData getAnimation(String name) {
        SpriteAnimationData animation = animations.get(name);
        if (animation == null) {
            logger.warn("Animation not found in registry: " + name);
        }
        return animation;
    }
    
    /**
     * Check if an asset is registered.
     */
    public boolean hasAsset(String name) {
        return metadata.containsKey(name);
    }
    
    /**
     * Check if an asset of specific type is registered.
     */
    public boolean hasAsset(String name, AssetType type) {
        AssetMetadata meta = metadata.get(name);
        return meta != null && meta.type == type;
    }
    
    /**
     * Get asset metadata.
     */
    public AssetMetadata getMetadata(String name) {
        return metadata.get(name);
    }
    
    /**
     * Unregister an asset.
     */
    public void unregisterAsset(String name) {
        AssetMetadata meta = metadata.get(name);
        if (meta != null) {
            switch (meta.type) {
                case IMAGE:
                    images.remove(name);
                    break;
                case ATLAS:
                    atlases.remove(name);
                    break;
                case SPRITE_SHEET:
                    spriteSheets.remove(name);
                    break;
                case TILESET:
                    tileSets.remove(name);
                    break;
                case MAP:
                    maps.remove(name);
                    break;
                case ANIMATION:
                    animations.remove(name);
                    break;
            }
            metadata.remove(name);
            
            logger.debug("Unregistered asset: " + name);
        }
    }
    
    /**
     * Get all registered asset names.
     */
    public String[] getAssetNames() {
        return metadata.keySet().toArray(new String[0]);
    }
    
    /**
     * Get all registered asset names of a specific type.
     */
    public String[] getAssetNames(AssetType type) {
        return metadata.entrySet().stream()
                .filter(entry -> entry.getValue().type == type)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }
    
    /**
     * Get registry statistics.
     */
    public RegistryStats getStats() {
        return new RegistryStats(
            images.size(),
            atlases.size(),
            spriteSheets.size(),
            tileSets.size(),
            maps.size(),
            animations.size()
        );
    }
    
    /**
     * Clear all registered assets.
     */
    public void clear() {
        images.clear();
        atlases.clear();
        spriteSheets.clear();
        tileSets.clear();
        maps.clear();
        animations.clear();
        metadata.clear();
        
        logger.info("Asset registry cleared");
    }
    
    /**
     * Initialize with common game assets.
     */
    public void initializeCommonAssets() {
        try {
            // Register player sprites
            registerSpriteSheet("player", "/rpg/assets/player/boy_down_1.png", 48, 48);
            
            // Register tile assets
            registerImage("grass", "/rpg/assets/tiles/grass.png");
            registerImage("wall", "/rpg/assets/tiles/wall.png");
            registerImage("water", "/rpg/assets/tiles/water.png");
            registerImage("earth", "/rpg/assets/tiles/earth.png");
            registerImage("tree", "/rpg/assets/tiles/tree.png");
            registerImage("sand", "/rpg/assets/tiles/sand.png");
            
            // Register maps
            registerMap("world01", "/rpg/assets/maps/world01.txt");
            
            logger.info("Common assets initialized");
            
        } catch (AssetLoadException e) {
            logger.error("Failed to initialize common assets", e);
        }
    }
    
    /**
     * Asset metadata class.
     */
    public static class AssetMetadata {
        public final String name;
        public final String path;
        public final AssetType type;
        public final long registrationTime;
        
        public AssetMetadata(String name, String path, AssetType type) {
            this.name = name;
            this.path = path;
            this.type = type;
            this.registrationTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Asset types.
     */
    public enum AssetType {
        IMAGE,
        ATLAS,
        SPRITE_SHEET,
        TILESET,
        MAP,
        ANIMATION,
        SOUND,
        MUSIC
    }
    
    /**
     * Registry statistics.
     */
    public static class RegistryStats {
        public final int imageCount;
        public final int atlasCount;
        public final int spriteSheetCount;
        public final int tileSetCount;
        public final int mapCount;
        public final int animationCount;
        public final int totalAssets;
        
        public RegistryStats(int imageCount, int atlasCount, int spriteSheetCount,
                           int tileSetCount, int mapCount, int animationCount) {
            this.imageCount = imageCount;
            this.atlasCount = atlasCount;
            this.spriteSheetCount = spriteSheetCount;
            this.tileSetCount = tileSetCount;
            this.mapCount = mapCount;
            this.animationCount = animationCount;
            this.totalAssets = imageCount + atlasCount + spriteSheetCount + 
                              tileSetCount + mapCount + animationCount;
        }
        
        @Override
        public String toString() {
            return "RegistryStats{" +
                   "images=" + imageCount +
                   ", atlases=" + atlasCount +
                   ", spriteSheets=" + spriteSheetCount +
                   ", tileSets=" + tileSetCount +
                   ", maps=" + mapCount +
                   ", animations=" + animationCount +
                   ", total=" + totalAssets +
                   '}';
        }
    }
}