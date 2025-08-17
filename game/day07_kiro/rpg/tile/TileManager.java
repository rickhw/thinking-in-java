package rpg.tile;

import rpg.Config;
import rpg.GamePanel;
import rpg.assets.GameMap;
import rpg.assets.TileSet;
import rpg.exceptions.AssetLoadException;
import rpg.utils.GameLogger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Enhanced TileManager that supports the new GameMap and TileSet system
 * while maintaining backward compatibility with the old system.
 */
public class TileManager {

    GamePanel gp;
    
    // Legacy support
    public Tile[] tiles;
    public int mapTileNum[][];
    
    // New enhanced system
    private GameMap currentMap;
    private TileSet tileSet;
    private boolean useEnhancedSystem = true;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        
        try {
            // Initialize enhanced system
            initializeEnhancedSystem();
        } catch (AssetLoadException e) {
            GameLogger.error("Failed to initialize enhanced tile system, falling back to legacy system", e);
            useEnhancedSystem = false;
            initializeLegacySystem();
        }
    }
    
    /**
     * Initialize the new enhanced tile and map system.
     */
    private void initializeEnhancedSystem() throws AssetLoadException {
        // Create default tileset
        tileSet = TileSet.createDefaultTileSet();
        
        // Load the current map
        currentMap = GameMap.loadFromTextFile("/rpg/assets/maps/world01.txt", tileSet);
        
        GameLogger.info("Enhanced tile system initialized successfully");
    }
    
    /**
     * Initialize legacy system for backward compatibility.
     */
    private void initializeLegacySystem() {
        tiles = new Tile[10];
        mapTileNum = new int[Config.MAX_WORLD_COL][Config.MAX_WORLD_ROW];

        getTileImage();
        loadMap("/rpg/assets/maps/world01.txt");
        
        GameLogger.info("Legacy tile system initialized");
    }

    public void getTileImage() {
        try {
            tiles[0] = new Tile();
            tiles[0].image = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/tiles/grass.png"));

            tiles[1] = new Tile();
            tiles[1].image = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/tiles/wall.png"));
            tiles[1].collision = true;

            tiles[2] = new Tile();
            tiles[2].image = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/tiles/water.png"));
            tiles[2].collision = true;

            tiles[3] = new Tile();
            tiles[3].image = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/tiles/earth.png"));

            tiles[4] = new Tile();
            tiles[4].image = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/tiles/tree.png"));
            tiles[4].collision = true;

            tiles[5] = new Tile();
            tiles[5].image = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/tiles/sand.png"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMap(String mapPath) {
        try {
            InputStream is = getClass().getResourceAsStream(mapPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while (col < Config.MAX_WORLD_COL && row < Config.MAX_WORLD_ROW) {
                String line = br.readLine();
                while (col < Config.MAX_WORLD_COL) {
                    String numbers[] = line.split(" ");
                    int num = Integer.parseInt(numbers[col]);
                    mapTileNum[col][row] = num;
                    col++;
                }
                if (col == Config.MAX_WORLD_COL) {
                    col = 0;
                    row++;
                }
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Update method for animated tiles.
     */
    public void update(float deltaTime) {
        if (useEnhancedSystem && currentMap != null) {
            currentMap.update(deltaTime);
        }
    }
    
    /**
     * Check if a tile is collidable at world coordinates.
     */
    public boolean isCollidable(int worldX, int worldY) {
        if (useEnhancedSystem && currentMap != null) {
            Point tileCoords = currentMap.worldToTile(worldX, worldY);
            return currentMap.isCollidable(tileCoords.x, tileCoords.y);
        } else {
            // Legacy collision check
            int col = worldX / Config.TILE_SIZE;
            int row = worldY / Config.TILE_SIZE;
            
            if (col >= 0 && col < Config.MAX_WORLD_COL && row >= 0 && row < Config.MAX_WORLD_ROW) {
                int tileNum = mapTileNum[col][row];
                if (tileNum >= 0 && tileNum < tiles.length && tiles[tileNum] != null) {
                    return tiles[tileNum].collision;
                }
            }
            return false;
        }
    }
    
    /**
     * Get tile ID at world coordinates.
     */
    public int getTileAt(int worldX, int worldY) {
        if (useEnhancedSystem && currentMap != null) {
            Point tileCoords = currentMap.worldToTile(worldX, worldY);
            return currentMap.getTile(tileCoords.x, tileCoords.y);
        } else {
            // Legacy tile access
            int col = worldX / Config.TILE_SIZE;
            int row = worldY / Config.TILE_SIZE;
            
            if (col >= 0 && col < Config.MAX_WORLD_COL && row >= 0 && row < Config.MAX_WORLD_ROW) {
                return mapTileNum[col][row];
            }
            return -1;
        }
    }

    public void draw(Graphics2D g2) {
        if (useEnhancedSystem && currentMap != null) {
            drawEnhanced(g2);
        } else {
            drawLegacy(g2);
        }
    }
    
    /**
     * Enhanced rendering using the new GameMap system.
     */
    private void drawEnhanced(Graphics2D g2) {
        // Calculate view bounds based on player position
        int viewX = gp.player.worldX - gp.player.screenX;
        int viewY = gp.player.worldY - gp.player.screenY;
        int viewWidth = Config.SCREEN_WIDTH;
        int viewHeight = Config.SCREEN_HEIGHT;
        
        Rectangle viewBounds = new Rectangle(viewX, viewY, viewWidth, viewHeight);
        currentMap.render(g2, viewBounds);
    }
    
    /**
     * Legacy rendering for backward compatibility.
     */
    private void drawLegacy(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < Config.MAX_WORLD_COL && worldRow < Config.MAX_WORLD_ROW) {
            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = worldCol * Config.TILE_SIZE;
            int worldY = worldRow * Config.TILE_SIZE;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            if (worldX + Config.TILE_SIZE > gp.player.worldX - gp.player.screenX &&
                    worldX - Config.TILE_SIZE < gp.player.worldX + gp.player.screenX &&
                    worldY + Config.TILE_SIZE > gp.player.worldY - gp.player.screenY &&
                    worldY - Config.TILE_SIZE < gp.player.worldY + gp.player.screenY) {

                if (tileNum >= 0 && tileNum < tiles.length && tiles[tileNum] != null) {
                    g2.drawImage(tiles[tileNum].image, screenX, screenY, Config.TILE_SIZE, Config.TILE_SIZE, null);
                }
            }
            worldCol++;

            if (worldCol == Config.MAX_WORLD_COL) {
                worldCol = 0;
                worldRow++;
            }
        }
    }
    
    // Enhanced system getters
    public GameMap getCurrentMap() { return currentMap; }
    public TileSet getTileSet() { return tileSet; }
    public boolean isUsingEnhancedSystem() { return useEnhancedSystem; }
    
    /**
     * Load a different map.
     */
    public void loadMap(String mapPath, boolean enhanced) {
        if (enhanced && useEnhancedSystem) {
            try {
                currentMap = GameMap.loadFromTextFile(mapPath, tileSet);
                GameLogger.info("Loaded enhanced map: " + mapPath);
            } catch (AssetLoadException e) {
                GameLogger.error("Failed to load enhanced map: " + mapPath, e);
            }
        } else {
            loadMap(mapPath); // Use legacy loading
        }
    }
}
