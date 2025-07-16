package rpg.entity;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Entity {
    // Position
    public int worldX, worldY;
    public int speed;

    // Animation
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public Direction direction;

    // Sprite animation
    public int spriteCounter = 0;
    public int spriteNum = 1;

    public Rectangle solidArea;
    public boolean collisionOn = false;

    public abstract void update();

    public abstract void draw(Graphics2D g2);
}
