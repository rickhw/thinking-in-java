package rpg.entity;

import rpg.Config;
import rpg.GamePanel;
import rpg.KeyHandler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyHandler;

    public final int screenX;
    public final int screenY;

    public Player(GamePanel gp, KeyHandler keyHandler) {
        this.gp = gp;
        this.keyHandler = keyHandler;

        screenX = Config.SCREEN_WIDTH / 2 - (Config.TILE_SIZE / 2);
        screenY = Config.SCREEN_HEIGHT / 2 - (Config.TILE_SIZE / 2);

        solidArea = new Rectangle(8, 16, 32, 32);

        setDefaultValues();
        getPlayerImages();
    }

    public void setDefaultValues() {
        worldX = Config.TILE_SIZE * 23;
        worldY = Config.TILE_SIZE * 21;
        speed = 5;
        direction = Direction.DOWN;
    }

    public void getPlayerImages() {
        try {
            up1 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_up_1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_up_2.png"));
            down1 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_down_1.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_down_2.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_left_1.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_left_2.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_right_1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/rpg/assets/player/boy_right_2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        if (keyHandler.upPressed || keyHandler.downPressed || keyHandler.leftPressed || keyHandler.rightPressed) {
            if (keyHandler.upPressed) {
                direction = Direction.UP;
            } else if (keyHandler.downPressed) {
                direction = Direction.DOWN;
            } else if (keyHandler.leftPressed) {
                direction = Direction.LEFT;
            } else if (keyHandler.rightPressed) {
                direction = Direction.RIGHT;
            }

            // check tile collision
            collisionOn = false;
            gp.collisionChecker.checkTile(this);

            // if no collision, move player
            if (!collisionOn) {
                switch (direction) {
                    case UP:
                        worldY -= speed;
                        break;
                    case DOWN:
                        worldY += speed;
                        break;
                    case LEFT:
                        worldX -= speed;
                        break;
                    case RIGHT:
                        worldX += speed;
                        break;
                }
            }

            // animation
            spriteCounter++;
            if (spriteCounter > 10) {
                if (spriteNum == 1) {
                    spriteNum = 2;
                } else if (spriteNum == 2) {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage image = null;
        switch (direction) {
            case UP:
                if (spriteNum == 1) image = up1;
                else image = up2;
                break;
            case DOWN:
                if (spriteNum == 1) image = down1;
                else image = down2;
                break;
            case LEFT:
                if (spriteNum == 1) image = left1;
                else image = left2;
                break;
            case RIGHT:
                if (spriteNum == 1) image = right1;
                else image = right2;
                break;
        }
        g2.drawImage(image, screenX, screenY, Config.TILE_SIZE, Config.TILE_SIZE, null);
    }
}