package rpg.tile;

import rpg.Config;
import rpg.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TileManager {

    GamePanel gp;
    public Tile[] tiles;
    public int mapTileNum[][];

    public TileManager(GamePanel gp) {
        this.gp = gp;
        tiles = new Tile[10];
        mapTileNum = new int[Config.MAX_WORLD_COL][Config.MAX_WORLD_ROW];

        getTileImage();
        loadMap("/rpg/assets/maps/world01.txt");
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


    public void draw(Graphics2D g2) {

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

                g2.drawImage(tiles[tileNum].image, screenX, screenY, Config.TILE_SIZE, Config.TILE_SIZE, null);
            }
            worldCol++;

            if (worldCol == Config.MAX_WORLD_COL) {
                worldCol = 0;
                worldRow++;
            }
        }
    }

}
