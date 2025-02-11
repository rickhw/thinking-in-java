package rpg.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import rpg.GamePanel;
import rpg.KeyHandler;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyHandler;

    public final int screenX; // camera position
    public final int screenY; // camera position

    public Player(GamePanel gp, KeyHandler keyHandler) {
        this.gp = gp;
        this.keyHandler = keyHandler;

        screenX = gp.screenWidth / 2 - (gp.tileSize /2); // camera position
        screenY = gp.screenHight / 2 - (gp.tileSize /2); // camera position

        setDefaultValues();
        getPlayerImages();
    }

    public void setDefaultValues() {
        worldX = gp.tileSize * 23;  // start position
        worldY = gp.tileSize * 21;  // start position
        speed = 5;
        direction = "down";
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

    public void update() {
        // if (keyHandler.upPressed || keyHandler.downPressed || keyHandler.leftPressed || keyHandler.rightPressed) {
            if(keyHandler.upPressed) {
                direction = "up";
                worldY -= speed;
            }
            if(keyHandler.downPressed) {
                direction = "down";
                worldY += speed;
            }
            if(keyHandler.leftPressed) {
                direction = "left";
                worldX -= speed;
            }
            if(keyHandler.rightPressed) {
                direction = "right";
                worldX += speed;
            }
    
            // animation 
            spriteCounter++;
            if(spriteCounter > 10) {
                if (spriteNum == 1) {
                    spriteNum = 2;
                } else if (spriteNum == 2) {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
        // }
    }

    public void draw(Graphics2D g2) {
        // g2.setColor(Color.white);
        // g2.fillRect(x, y, gp.tileSize, gp.tileSize);

        BufferedImage image = up1;
        switch(direction) {
            case "up":
                if(spriteNum == 1) {
                    image = up1;
                } 
                if (spriteNum == 2) {
                    image = up2;
                }
                break;
            case "down":
                if (spriteNum == 1) {
                    image = down1;
                }
                if (spriteNum == 2) {
                    image = down2;
                }
                break;
            case "left":
                if (spriteNum == 1) {
                    image = left1;
                }
                if (spriteNum == 2) {
                    image = left2;
                }
                break;
            case "right":
                if (spriteNum == 1) {
                    image = right1;
                }
                if (spriteNum == 2) {
                    image = right2;
                }
                break;
        }

        g2.drawImage(image, screenX, screenX, gp.tileSize, gp.tileSize, null);    }

}