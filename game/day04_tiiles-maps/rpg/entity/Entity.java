package rpg.entity;

import java.awt.image.BufferedImage;

public class Entity {
    // Position
    public int x, y;
    public int speed;

    // Animation
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public String direction;

    // Sprite animation
    public int spriteCounter = 0;
    public int spriteNum = 1;
}
