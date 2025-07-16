package rpg;

import rpg.entity.Entity;

public class CollisionChecker {
    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(Entity entity) {
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;

        int entityLeftCol = entityLeftWorldX / Config.TILE_SIZE;
        int entityRightCol = entityRightWorldX / Config.TILE_SIZE;
        int entityTopRow = entityTopWorldY / Config.TILE_SIZE;
        int entityBottomRow = entityBottomWorldY / Config.TILE_SIZE;

        int tileNum1, tileNum2;

        switch (entity.direction) {
            case UP:
                entityTopRow = (entityTopWorldY - entity.speed) / Config.TILE_SIZE;
                if (entityTopRow < 0 || entityLeftCol < 0 || entityRightCol >= Config.MAX_WORLD_COL) {
                    entity.collisionOn = true;
                    break;
                }
                tileNum1 = gp.tileManager.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileManager.mapTileNum[entityRightCol][entityTopRow];
                if (gp.tileManager.tiles[tileNum1].collision || gp.tileManager.tiles[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;
            case DOWN:
                entityBottomRow = (entityBottomWorldY + entity.speed) / Config.TILE_SIZE;
                if (entityBottomRow >= Config.MAX_WORLD_ROW || entityLeftCol < 0 || entityRightCol >= Config.MAX_WORLD_COL) {
                    entity.collisionOn = true;
                    break;
                }
                tileNum1 = gp.tileManager.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = gp.tileManager.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileManager.tiles[tileNum1].collision || gp.tileManager.tiles[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;
            case LEFT:
                entityLeftCol = (entityLeftWorldX - entity.speed) / Config.TILE_SIZE;
                if (entityLeftCol < 0 || entityTopRow < 0 || entityBottomRow >= Config.MAX_WORLD_ROW) {
                    entity.collisionOn = true;
                    break;
                }
                tileNum1 = gp.tileManager.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileManager.mapTileNum[entityLeftCol][entityBottomRow];
                if (gp.tileManager.tiles[tileNum1].collision || gp.tileManager.tiles[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;
            case RIGHT:
                entityRightCol = (entityRightWorldX + entity.speed) / Config.TILE_SIZE;
                if (entityRightCol >= Config.MAX_WORLD_COL || entityTopRow < 0 || entityBottomRow >= Config.MAX_WORLD_ROW) {
                    entity.collisionOn = true;
                    break;
                }
                tileNum1 = gp.tileManager.mapTileNum[entityRightCol][entityTopRow];
                tileNum2 = gp.tileManager.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileManager.tiles[tileNum1].collision || gp.tileManager.tiles[tileNum2].collision) {
                    entity.collisionOn = true;
                }
                break;
        }
    }
}
