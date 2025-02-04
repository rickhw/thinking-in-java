package rpg.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import rpg.GamePanel;
import rpg.KeyHandler;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyHandler;

    public Player(GamePanel gp, KeyHandler keyHandler) {
        this.gp = gp;
        this.keyHandler = keyHandler;
        setDefaultValues();
        getPlayerImages();
    }

    public void setDefaultValues() {
        x = 100;
        y = 100;
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
                y -= speed;
            }
            if(keyHandler.downPressed) {
                direction = "down";
                y += speed;
            }
            if(keyHandler.leftPressed) {
                direction = "left";
                x -= speed;
            }
            if(keyHandler.rightPressed) {
                direction = "right";
                x += speed;
            }
    
            // animation 
            spriteCounter++;
            if(spriteCounter > 30) {
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

        g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);    }

}